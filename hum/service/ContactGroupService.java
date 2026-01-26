package ita.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import io.micrometer.tracing.Tracer;
import ita.dto.*;
import ita.entity.CampaignDetail;
import ita.entity.Contact;
import ita.entity.ContactGroup;
import ita.entity.Content;
import ita.enumeration.EntityType;
import ita.exception.NotFoundException;
import ita.repository.ContactGroupRepository;
import ita.specification.ContactGroupSpecification;
import ita.util.AuthUtil;
import ita.util.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;
import static ita.enumeration.EntityType.CONTACT_GROUP_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;

@Service
@Slf4j
public class ContactGroupService {

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Tracer tracer;

    private final ContactGroupRepository contactGroupRepository;
    private final ContactService contactService;

    public ContactGroupService(ContactGroupRepository contactGroupRepository, ContactService contactService) {
        this.contactGroupRepository = contactGroupRepository;
        this.contactService = contactService;
    }

    public Page<ContactGroupResponseDto> findAll(BaseSearchCriteriaDto searchCriteria) {
        Pageable pageable;
        int pageSize;

        if (searchCriteria.getSize() == 999) {
            pageSize = Integer.MAX_VALUE;
        } else {
            pageSize = searchCriteria.getSize();
        }

        if (searchCriteria.getType().equals("desc")) {
            pageable = PageRequest.of(searchCriteria.getPage(), pageSize, Sort.by(searchCriteria.getParam()).descending());
        } else {
            pageable = PageRequest.of(searchCriteria.getPage(), pageSize, Sort.by(searchCriteria.getParam()).ascending());
        }

        UUID paramId = null;

        if (!searchCriteria.getId().isBlank()) {
            paramId = UUID.fromString(searchCriteria.getId());
        }

        Specification<ContactGroup> contactGroupSpecification = Specification.where(ContactGroupSpecification.idLike(paramId))
                .and(ContactGroupSpecification.nameLike(searchCriteria.getName()));

        Page<ContactGroup> contactGroups = contactGroupRepository.findAll(contactGroupSpecification, pageable);

        List<ContactGroupResponseDto> contactGroupResponseDtos = contactGroups.getContent().stream().map(contactGroup ->
                ContactGroupResponseDto.builder()
                        .id(contactGroup.getId())
                        .name(contactGroup.getName())
                        .build()).toList();

        return new PageImpl<>(contactGroupResponseDtos, pageable, contactGroups.getTotalElements());
    }

    public ContactGroup addContactGroup(ContactGroupRequestDto contactGroupRequestDto) {
        ContactGroup contactGroup = new ContactGroup();
        List<Contact> contacts = contactService.findAllByIds(contactGroupRequestDto.getContactIds());

        contactGroup.setName(contactGroupRequestDto.getName());
        contactGroup.setContacts(contacts);
        contactGroup.setCreatedAt(System.currentTimeMillis());

        return contactGroupRepository.save(contactGroup);
    }

    public ContactGroup updateContactGroup(ContactGroupUpdateDto contactGroupUpdateDto) {
        ContactGroup contactGroupFromDb = findById(contactGroupUpdateDto.getId());
        List<Contact> contacts = contactService.findAllByIds(contactGroupUpdateDto.getContactIds());

        contactGroupFromDb.setName(contactGroupUpdateDto.getName());
        contactGroupFromDb.setContacts(contacts);
        contactGroupFromDb.setUpdatedAt(System.currentTimeMillis());

        return contactGroupRepository.save(contactGroupFromDb);
    }

    public void deleteContactGroup(String id) {
        UUID contactGroupId = UUID.fromString(id);

        contactGroupRepository.deleteById(contactGroupId);
    }

    public ContactGroup addContactBulk(ContactBulkRequestDto contactBulkRequestDto) throws IOException, NoSuchMethodException, MethodArgumentNotValidException {
        List<String> contactEmails = new ArrayList<>();
        Reader reader = new InputStreamReader(contactBulkRequestDto.getFile().getInputStream());
        CSVReader csvReader = new CSVReader(reader);
        csvReader.skip(1);

        Method method = this.getClass().getDeclaredMethod("addContactBulk", ContactBulkRequestDto.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        String groupName = contactBulkRequestDto.getFile().getOriginalFilename();

        if (groupName == null || groupName.isBlank()) {
            BeanPropertyBindingResult result = new BeanPropertyBindingResult(contactBulkRequestDto, "name");

            result.addError(new FieldError("name",
                    "name",
                    "Must be filled"));

            throw new MethodArgumentNotValidException(parameter, result);
        }

        try {
            String[] values;

            while ((values = csvReader.readNext()) != null) {
                String email = values[0];

                if (!EmailUtil.isEmailAddressValid(email)) {
                    BeanPropertyBindingResult result = new BeanPropertyBindingResult(contactBulkRequestDto, "file");

                    result.addError(new FieldError("file",
                            "file",
                            "Invalid email format."));

                    throw new MethodArgumentNotValidException(parameter, result);
                }

                contactEmails.add(email);
            }

        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        Set<String> uniqueEmail = new HashSet<>(contactEmails);

        List<Contact> existingContacts = contactService.findByEmailIn(uniqueEmail);

        Map<String, Contact> existingContactMap = existingContacts.stream()
                .collect(Collectors.toMap(Contact::getEmail, Function.identity()));

        List<Contact> finalContacts = new ArrayList<>();
        List<Contact> contactsToCreate = new ArrayList<>();

        for (String email : contactEmails) {
            Contact contact = existingContactMap.get(email);

            if (contact != null) {
                finalContacts.add(contact);
            } else {
                Contact newContact = new Contact();

                newContact.setName(cleanFormat(email));
                newContact.setEmail(email);
                newContact.setCreatedAt(System.currentTimeMillis());
                newContact.setIsUnsubscribed(false);

                contactsToCreate.add(newContact);
            }
        }

        if (!contactsToCreate.isEmpty()) {
            List<Contact> newlyCreatedContacts = contactService.addContactBulk(contactsToCreate);

            finalContacts.addAll(newlyCreatedContacts);
        }

        ContactGroup contactGroup = new ContactGroup();

        contactGroup.setContacts(finalContacts);
        contactGroup.setName(contactBulkRequestDto.getFile().getOriginalFilename());
        contactGroup.setCreatedAt(System.currentTimeMillis());

        MDC.put("entity_type", CONTACT_GROUP_TYPE.getValue());
        MDC.put("operation_type", ADD_OPERATION.getValue());
        MDC.put("method", method.getName());
        MDC.put("service", serviceName);
        MDC.put("trace_id", Objects.requireNonNull(tracer.currentTraceContext().context()).traceId());

        MDC.remove("traceId");
        MDC.remove("spanId");

        String name = AuthUtil.getUsername();
        Class<?> clazz = this.getClass();
        String simpleName = clazz.getSimpleName();

        MDC.put("user", name);
        MDC.put("actual_class", simpleName);

        log.info("Request Body: {}", contactGroup);

        return contactGroupRepository.save(contactGroup);
    }

    public ContactGroup findById(UUID id) {
        Optional<ContactGroup> contactGroup = contactGroupRepository.findById(id);

        if (contactGroup.isEmpty()) {
            throw new NotFoundException(EntityType.CONTACT_GROUP_TYPE, "id", id.toString());
        }

        return contactGroup.get();
    }

    public ByteArrayResource export(UUID id) {
        ContactGroup contactGroup = findById(id);

        try (Workbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("List of Contact");
            Row headerRow = sheet.createRow(0);

            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Contact Email");

            for (int i = 0; i < contactGroup.getContacts().size(); i++) {
                Row row = sheet.createRow(i + 1);

                Cell cell = row.createCell(0);

                cell.setCellValue(contactGroup.getContacts().get(i).getEmail());
            }

            sheet.autoSizeColumn(0);

            workbook.write(outputStream);

            byte[] bytes = outputStream.toByteArray();

            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean existsByName(String name) {
        return contactGroupRepository.existsByName(name);
    }

    private String cleanFormat(String email) {
        String name = email.split("@")[0];

        if (name.contains(".") || name.contains("_")) {
            name = name.replace(".", " ");
            name = name.replace("_", " ");
        }

        return name;
    }

}
