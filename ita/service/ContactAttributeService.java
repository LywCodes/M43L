package ita.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ita.dto.ContactAttributeResponseDto;
import ita.dto.ContactBulkRequestDto;
import ita.entity.Contact;
import ita.entity.ContactAttribute;
import ita.entity.ContactGroup;
import ita.repository.ContactAttributeRepository;
import ita.repository.ContactGroupRepository;
import ita.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContactAttributeService {

    private final ContactAttributeRepository contactAttributeRepository;
    private final ContactGroupRepository contactGroupRepository;
    private final ContactService contactService;
    private static final int CHUNK_SIZE = 20000;
    private static final int BATCH_SIZE = 10000;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importContactAttribute(ContactBulkRequestDto contactBulkRequestDto) throws IOException, CsvValidationException {
// todo: create exception handler
        int totalProcessed = 0;
        Set<String> globalProcessedEmails = new HashSet<>();
        List<Contact> groupMembers = new ArrayList<>();


        if (Boolean.TRUE.equals(contactGroupRepository.existsByName(contactBulkRequestDto.getFile().getOriginalFilename()))) {
            throw new IllegalArgumentException("Group Name '" + contactBulkRequestDto.getFile().getOriginalFilename()
                    + "' already exists. Please use a different name.");
        }

        ContactGroup newGroup = new ContactGroup();
        newGroup.setName(contactBulkRequestDto.getFile().getOriginalFilename());
        newGroup.setCreatedAt(System.currentTimeMillis());
        ContactGroup savedGroup = contactGroupRepository.save(newGroup);

        try (Reader reader = new InputStreamReader(contactBulkRequestDto.getFile().getInputStream());
             CSVReader csvReader = new CSVReader(reader)) {

            String[]headers = csvReader.readNext();
            if (headers == null) throw new RuntimeException("CSV is empty");

            int emailIndex = findEmailIndex(headers);
            List<String[]> batchRows = new ArrayList<>();
            String[] row;

            while ((row = csvReader.readNext()) != null){
                batchRows.add(row);

                if (batchRows.size()>=CHUNK_SIZE){
                    log.info("Chunk filled, Size: {}", batchRows.size());
                    List<Contact>processed = processChunk(batchRows, headers, emailIndex,savedGroup, globalProcessedEmails);

                    groupMembers.addAll(processed);
                    totalProcessed += processed.size();
                    batchRows.clear(); //clear memory

                    log.info("chunk cleared");
                }
            }

            if (!batchRows.isEmpty()){
                List<Contact> processed = processChunk(batchRows, headers, emailIndex, savedGroup, globalProcessedEmails);
                groupMembers.addAll(processed);
                totalProcessed += processed.size();
            }
        }

        if (groupMembers.isEmpty()){
            throw new RuntimeException("No valid contacts found in CSV file");
        }

        savedGroup.setContacts(groupMembers);
        contactGroupRepository.save(savedGroup);

        return buildResponse(contactBulkRequestDto.getFile().getOriginalFilename(), totalProcessed);
    }

    private List<Contact> processChunk(List<String[]> rows,
                                       String[] headers,
                                       int emailIndex,
                                       ContactGroup group,
                                       Set<String> globalProcessedEmails) {

        //  Filter & Deduplicate
        Map<String, String[]> emailToRowMap = rows.stream()
                .filter(row -> row.length > emailIndex && EmailUtil.isEmailAddressValid(row[emailIndex]))
                .filter(row -> {
                    String email = row[emailIndex];
                    if (globalProcessedEmails.contains(email)) {
                        log.warn("Skipped Duplicate Email (Global): {}", email);
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toMap(
                        row -> row[emailIndex],
                        Function.identity(),
                        (existing, replacement) -> {
                            log.warn("Duplicate email found within chunk: '{}'. Using latest.", existing[emailIndex]);
                            return replacement;
                        }
                ));

        if (emailToRowMap.isEmpty()) return Collections.emptyList();

        // Update  Tracker
        globalProcessedEmails.addAll(emailToRowMap.keySet());
        Set<String> uniqueEmails = emailToRowMap.keySet();

        //  Upsert Contact
        List<Contact> existingContacts = contactService.findByEmailIn(uniqueEmails);

        Map<String, Contact> contactMap = existingContacts.stream()
                .collect(Collectors.toMap(Contact::getEmail, Function.identity()));

        List<Contact> newContacts = uniqueEmails.stream()
                .filter(email -> !contactMap.containsKey(email))
                .map(email -> {
                    Contact c = new Contact();
                    c.setEmail(email);
                    c.setName(email.split("@")[0]);
                    c.setCreatedAt(System.currentTimeMillis());
                    c.setIsUnsubscribed(false);
                    return c;
                })
                .toList();

        if (!newContacts.isEmpty()) {
            contactService.addContactBulk(newContacts)
                    .forEach(c -> contactMap.put(c.getEmail(), c));
        }

        //  Mapping Attribute
        List<ContactAttribute> attributesToSave = emailToRowMap.entrySet().stream()
                .map(entry -> {
                    String email = entry.getKey();
                    String[] row = entry.getValue();
                    Contact contact = contactMap.get(email);

                    if (contact == null) return null;

                    Map<String, String> jsonAttributes = IntStream.range(0, headers.length)
                            .filter(i -> i != emailIndex)
                            .filter(i -> !headers[i].trim().isEmpty())
                            .boxed()
                            .collect(Collectors.toMap(
                                    i -> headers[i].trim().toLowerCase(),
                                    i ->{
                                        String val = (i < row.length && row[i] != null) ? row[i] : "";
                                        return val.trim();},
                                    (v1, v2) -> v2
                            ));

                    return ContactAttribute.builder()
                            .contact(contact)
                            .contactGroup(group)
                            .attributes(jsonAttributes)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
        log.info("Ready to insert {} attributes, splitting into batches of {}", attributesToSave.size(), BATCH_SIZE);

        // Save Attribute (Partitioned Insert)
        if (!attributesToSave.isEmpty()) {
            int batchCount = 0;
            for (int i = 0; i < attributesToSave.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, attributesToSave.size());

                batchCount++;
                log.info("saving batch #{}: records {} to {}", batchCount, i, end);

                List<ContactAttribute> batch = attributesToSave.subList(i, end);
                contactAttributeRepository.saveAll(batch);
            }
        }
        return new ArrayList<>(contactMap.values());
    }

    private Map<String, Object> buildResponse(String groupName, int total) {
        Map<String, Object> response = new HashMap<>();

        response.put("groupName", groupName);
        response.put("totalData", total);

        return response;
    }

    private int findEmailIndex(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].trim().equalsIgnoreCase("EMAIL")) return i;
        }
        throw new RuntimeException("Header 'EMAIL' is required");
    }

    public Set<String> findAllDynamicKeys(Long startDate, long endDate){
        return contactAttributeRepository.findAllDynamicKeys(startDate, endDate);
    }

    public Map<String, Map<String, String>> getAttributesMap(List<UUID> contactIds) {
        return contactAttributeRepository.findByContactIdIn(contactIds)
                .stream()
                .collect(Collectors.toMap(
                        attr -> attr.getContactGroup().getId().toString() + "_" + attr.getContact().getId().toString(),
                        ContactAttribute::getAttributes,
                        (existing, replacement) -> existing
                ));
    }

    public Optional<ContactAttribute>getSampleByGroupId(UUID groupId){
        return contactAttributeRepository.findFirstByContactGroupId(groupId);
    }

    public Map<UUID, Map<String, String>> getAttributesMapByGroupId(UUID groupId) {
        List<ContactAttribute> allAttributes = contactAttributeRepository.findByContactGroupId(groupId);

        return allAttributes.stream()
                .collect(Collectors.toMap(
                        attr -> attr.getContact().getId(),
                        ContactAttribute::getAttributes,
                        (existing, replacement) -> existing // Penanganan jika ada duplikat ID
                ));
    }

    //  GET attributes
    public List<ContactAttributeResponseDto> getAttributesByGroupId(UUID groupId) {

        List<ContactAttribute> attributes = contactAttributeRepository.findByContactGroupId(groupId);

        return attributes.stream()
                .map(attr -> ContactAttributeResponseDto.builder()
                        .id(attr.getId())
                        .contactId(attr.getContact().getId())
                        .email(attr.getContact().getEmail())
                        .name(attr.getContact().getName())
                        .attributes(attr.getAttributes()) // Map JSONB
                        .build())
                        .toList();
    }
}
