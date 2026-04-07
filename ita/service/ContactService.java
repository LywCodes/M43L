package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContactRequestDto;
import ita.dto.ContactUpdateDto;
import ita.entity.Contact;
import ita.exception.NotFoundException;
import ita.repository.ContactRepository;
import ita.specification.ContactSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending());

        Specification<Contact> contactSpecification = Specification.allOf(
                ContactSpecification.nameLike(searchCriteria.getName())
                        .and(ContactSpecification.emailLike(searchCriteria.getEmail()))
        );

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

    public Contact updateContact(ContactUpdateDto contactUpdateDto) {
        Contact contact = findById(contactUpdateDto.getId());

        contact.setName(contactUpdateDto.getName());
        contact.setEmail(contactUpdateDto.getEmail());
        contact.setUpdatedAt(System.currentTimeMillis());

        return contactRepository.save(contact);
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

    public boolean isUniqueForUpdate(String email, UUID id) {
        return !contactRepository.existsByEmailAndNotId(email, id);
    }
}
