package ita.service;

import ita.dto.*;
import ita.entity.LocalRole;
import ita.entity.LocalUser;
import ita.entity.UserDetailsImpl;
import ita.exception.LdapErrorHandler;
import ita.exception.NotFoundException;
import ita.repository.UserRepository;
import ita.specification.UserSpecification;
import ita.util.AuthUtil;
import ita.util.JwtUtil;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

import static ita.enumeration.EntityType.USER_TYPE;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RoleService roleService;
    private final RestTemplate restTemplate;
    private final SsoService ssoService;
    private final SecretKey secretKey;
    private final String serviceUrl;
    private final String clientId;
    private final String appId;

    private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    public UserService(UserRepository userRepository,
                       JwtUtil jwtUtil,
                       RoleService roleService,
                       RestTemplate restTemplate,
                       SsoService ssoService,
                       SecretKey secretKey,
                       @Value("${eai.api.url}") String serviceUrl,
                       @Value("${eai.client.id}")  String clientId,
                       @Value("${eai.api.app.id}")   String appId) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.roleService = roleService;
        this.restTemplate = restTemplate;
        this.ssoService = ssoService;
        this.secretKey = secretKey;
        this.serviceUrl = serviceUrl;
        this.clientId = clientId;
        this.appId = appId;
    }

    public Page<UserResponseDto> findAllUser(BaseSearchCriteriaDto searchCriteria) {
        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteria.getPage(),
                searchCriteria.getSize(),
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending()
        );

        Specification<LocalUser> userSpecification = Specification.allOf(UserSpecification.nameLike(searchCriteria.getName())
                .and(UserSpecification.usernameLike(searchCriteria.getUsername()))
        );

        return userRepository.findAll(userSpecification, pageable)
                .map(user -> UserResponseDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .build());
    }

    public LocalUser findById(UUID id) {
        Optional<LocalUser> user = userRepository.findById(id);

        if (user.isEmpty()) throw new NoSuchElementException(String.format("User with id %s not found", id));

        return user.get();
    }

    public String getUsernameById(UUID id) {
        if(id.toString().equals("00000000-0000-0000-0000-000000000000")){
            return "SYSTEM";
        }
        try{
            return findById(id).getUsername();
        }catch (NoSuchElementException e){
            return "unknown user";
        }
    }

    public LocalUser findByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username can't be null or empty");
        }

        Optional<LocalUser> userFromDb = userRepository.findByUsernameIgnoreCase(username);

        return userFromDb
                .orElseThrow(() -> new NotFoundException(USER_TYPE, "username", username));
    }

    public LocalUser addUser(UserRequestDto userRequestDto) {
        if (Boolean.TRUE.equals(existsByUsername(userRequestDto.getUsername()))) {
            throw new InputMismatchException(String.format("User with username %s already exist", userRequestDto.getUsername()));
        }

        Set<LocalRole> roles = new HashSet<>(roleService.findAllById(userRequestDto.getRoleIds()));

        LocalUser user = new LocalUser();

        user.setName(userRequestDto.getName());
        user.setUsername(userRequestDto.getUsername());
        user.setRoles(roles);
        user.setCreatedAt(System.currentTimeMillis());

        return userRepository.save(user);
    }

    public LocalUser updateUser(UserUpdateDto userUpdateDto) {
        LocalUser user = findById(userUpdateDto.getId());
        Set<LocalRole> roles = new HashSet<>(roleService.findAllById(userUpdateDto.getRoleIds()));

        user.setUpdatedAt(System.currentTimeMillis());
        user.setRoles(roles);
        user.setName(userUpdateDto.getName());
        user.setUsername(userUpdateDto.getUsername());

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        UUID userId = UUID.fromString(id);

        userRepository.deleteById(userId);
    }

    public JwtResponseDto authenticateUser(LoginRequestDto loginRequestDto) {
        HttpHeaders headers = new HttpHeaders();

        String transactionId = "NeoMail-" + UUID.randomUUID();

        headers.add("Authorization", "Bearer " + ssoService.getSsoToken());
        headers.add("x-source-client-id", clientId);
        headers.add("x-source-transaction-id", transactionId);
        headers.setContentType(MediaType.APPLICATION_JSON);

        LdapRequestDto ldapRequestDto = new LdapRequestDto();

        ldapRequestDto.setApplicationId(appId);
        ldapRequestDto.setUserId(loginRequestDto.getUsername());
        ldapRequestDto.setPassword(securePassword(loginRequestDto.getPassword()));

        HttpEntity<LdapRequestDto> request = new HttpEntity<>(ldapRequestDto, headers);

        restTemplate.setErrorHandler(new LdapErrorHandler());
        restTemplate.postForEntity(serviceUrl, request, String.class);

        LocalUser user = findByUsername(loginRequestDto.getUsername());

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String jwt = jwtUtil.generateJwt(userDetails);

        List<String> permissions = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();



        return JwtResponseDto.builder()
                .jwt(jwt)
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .permissions(permissions)
                .build();
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isUniqueForUpdate(String username, UUID id) {
        return !userRepository.existsByUsernameAndNotId(username, id);
    }

    private String securePassword(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] plainTextBytes = plainText.getBytes();
            byte[] encryptedBytes = cipher.doFinal(plainTextBytes);

            return DatatypeConverter.printHexBinary(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data: " + e.getMessage(), e);
        }
    }

    public List<ApproverSelectionDto> getApprover() {
        UUID currentUserId = AuthUtil.getUserId();

        List<LocalUser> approvers = userRepository.findApprovers("APPROVER", currentUserId);

        return approvers.stream()
                .map(user -> new ApproverSelectionDto(user.getId(), user.getUsername()))
                .collect(Collectors.toList());
    }

}
