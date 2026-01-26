package ita.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContactBulkRequestDto;
import ita.dto.ContactRequestDto;
import ita.dto.ContactUpdateDto;
import ita.entity.Contact;
import ita.entity.ContactGroup;
import ita.exception.NotFoundException;
import ita.repository.ContactRepository;
import ita.specification.ContactGroupSpecification;
import ita.specification.ContactSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

import static ita.enumeration.EntityType.CONTACT_TYPE;

@Service
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;

    public ContactService(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    public Page<Contact> findAllContact(BaseSearchCriteriaDto searchCriteria) {
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

        Specification<Contact> contactSpecification = Specification.where(ContactSpecification.nameLike(searchCriteria.getName()))
                .and(ContactSpecification.emailLike(searchCriteria.getEmail()));

        return contactRepository.findAll(contactSpecification, pageable);
    }

    public List<Contact> findAllByIds(List<UUID> ids) {
        return contactRepository.findAllById(ids);
    }

    public Contact addContact(ContactRequestDto contactRequestDto) {
        Contact contact = new Contact();

        contact.setName(contactRequestDto.getName());
        contact.setEmail(contactRequestDto.getEmail());
        contact.setCreatedAt(System.currentTimeMillis());
        contact.setIsUnsubscribed(false);

        return  contactRepository.save(contact);
    }

//    public List<Contact> addContactBulk(ContactBulkRequestDto contactBulkRequestDto) throws IOException {
//        List<Contact> newContacts = new ArrayList<>();
//        Reader reader = new InputStreamReader(contactBulkRequestDto.getFile().getInputStream());
//        CSVReader csvReader = new CSVReader(reader);
//        csvReader.skip(1);
//
//        try {
//            String[] values;
//            while ((values = csvReader.readNext()) != null) {
//                String email = values[0];
//
//                String name = cleanFormat(email);
//
//                Contact contact = new Contact();
//
//                contact.setName(name);
//                contact.setEmail(email);
//                contact.setCreatedAt(System.currentTimeMillis());
//
//                newContacts.add(contact);
//            }
//
//        } catch (CsvValidationException e) {
//            throw new RuntimeException(e);
//        }
//
//        return contactRepository.saveAll(newContacts);
//    }

    public List<Contact> addContactBulk(List<Contact> contacts) {
        Set<Contact> uniqueContacts = new HashSet<>(contacts);

        return contactRepository.saveAll(uniqueContacts);
    }

    public List<Contact> findByEmailIn(Set<String> emails) {
        return contactRepository.findByEmailIn(emails);
    }

    public Contact findById(UUID id) {
        Optional<Contact> contact = contactRepository.findById(id);

        if (contact.isEmpty()) {
            throw new NotFoundException(CONTACT_TYPE, "id", id.toString());
        }

        return contact.get();
    }

    public Contact updateContact(ContactUpdateDto contact) {
        Contact contactFromDB = findById(contact.getId());

        contactFromDB.setName(contact.getName());
        contactFromDB.setEmail(contact.getEmail());
        contactFromDB.setUpdatedAt(System.currentTimeMillis());

        return contactRepository.save(contactFromDB);
    }

    public Contact updateContactStatus(Contact contact) {
        return contactRepository.save(contact);
    }

    @Transactional
    public void deleteContact(String id) {
        UUID contactId = UUID.fromString(id);

        contactRepository.deleteGroupContactRelations(contactId);

        contactRepository.deleteById(contactId);
    }

    public Boolean existsByEmail(String email) {
        return contactRepository.existsByEmail(email);
    }

//    private String cleanFormat(String email) {
//        String name = email.split("@")[0];
//
//        if (name.contains(".") || name.contains("_")) {
//            name = name.replace(".", " ");
//            name = name.replace("_", " ");
//        }
//
//        return name;
//    }

}
