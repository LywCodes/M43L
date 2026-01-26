package ita.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ita.dto.*;
import ita.entity.LocalRole;
import ita.entity.LocalUser;
import ita.entity.Sender;
import ita.entity.UserDetailsImpl;
import ita.exception.LdapErrorHandler;
import ita.exception.NotFoundException;
import ita.repository.UserRepository;
import ita.specification.SenderSpecification;
import ita.specification.UserSpecification;
import ita.util.JwtUtil;
import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ita.enumeration.EntityType.USER_TYPE;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final RestTemplate restTemplate;
    private final SsoService ssoService;

    @Autowired
    private SecretKey secretKey;

    private static final String TRANSFORMATION = "DESede/ECB/PKCS5Padding";

    @Value("${eai.api.url}")
    private String serviceUrl;

    @Value("${eai.client.id}")
    private String clientId;

    @Value("${eai.api.app.id}")
    private String appId;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, RoleService roleService, RestTemplate restTemplate, SsoService ssoService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.restTemplate = restTemplate;
        this.ssoService = ssoService;
    }

    public Page<UserResponseDto> findAllUser(BaseSearchCriteriaDto searchCriteria) {
        Pageable pageable;

        if (searchCriteria.getType().equals("desc")) {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), Sort.by(searchCriteria.getParam()).descending());
        } else {
            pageable = PageRequest.of(searchCriteria.getPage(), searchCriteria.getSize(), Sort.by(searchCriteria.getParam()).ascending());
        }

        Specification<LocalUser> userSpecification = Specification.where(UserSpecification.nameLike(searchCriteria.getName()))
                .and(UserSpecification.usernameLike(searchCriteria.getUsername()));

        Page<LocalUser> localUsers = userRepository.findAll(userSpecification, pageable);

        List<UserResponseDto> userResponseDtos = localUsers.getContent().stream().map(user ->
                UserResponseDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .build()).toList();

        return new PageImpl<>(userResponseDtos, pageable, localUsers.getTotalElements());
    }



    public LocalUser findById(UUID id) {
        Optional<LocalUser> user = userRepository.findById(id);

        if (user.isEmpty()) throw new NoSuchElementException(String.format("User with id %s not found", id));

        return user.get();
    }

    public LocalUser findByUsername(String username) {
        Optional<LocalUser> userFromDb = userRepository.findByUsername(username);

        if (username.isEmpty()) {
            throw new NotFoundException(USER_TYPE, "username", username);
        }

        return userFromDb.get();
    }

    public LocalUser addUser(UserRequestDto userRequestDto) {
        if (existsByUsername(userRequestDto.getUsername())) {
            throw new InputMismatchException(String.format("User with username %s already exist", userRequestDto.getUsername()));
        }

        Set<LocalRole> roles = new HashSet<>(roleService.findAllById(userRequestDto.getRoleIds()));

//        userRequestDto.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));

        LocalUser user = new LocalUser();

        user.setName(userRequestDto.getName());
        user.setUsername(userRequestDto.getUsername());
//        user.setPassword(userRequestDto.getPassword());
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

    public void changePassword(ChangePasswordRequestDto changePasswordRequestDto) {
        LocalUser user = findById(changePasswordRequestDto.getId());

        user.setPassword(passwordEncoder.encode(changePasswordRequestDto.getNewPassword()));
        user.setUpdatedAt(System.currentTimeMillis());

        userRepository.save(user);
    }

    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
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

}
