package ita.service;

import ita.dto.BaseSearchCriteriaDto;
import ita.dto.SenderRequestDto;
import ita.dto.SenderUpdateDto;
import ita.entity.Sender;
import ita.exception.NotFoundException;
import ita.repository.SenderRepository;
import ita.specification.SenderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.SENDER_TYPE;

@Service
public class SenderService {

    private final SenderRepository senderRepository;

    public SenderService(SenderRepository senderRepository) {
        this.senderRepository = senderRepository;
    }

    public Page<Sender> findAllSender(BaseSearchCriteriaDto searchCriteriaDto) {
        int pageSize = (searchCriteriaDto.getSize() == 999) ? Integer.MAX_VALUE : searchCriteriaDto.getSize();

        Sort sort = Sort.by(searchCriteriaDto.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteriaDto.getPage(),
                pageSize,
                "desc".equalsIgnoreCase(searchCriteriaDto.getType()) ? sort.descending() : sort.ascending()
        );

        Specification<Sender> senderSpecification = Specification.allOf(
                SenderSpecification.nameLike(searchCriteriaDto.getName())
                        .and(SenderSpecification.emailLike(searchCriteriaDto.getEmail()))
        );

        return senderRepository.findAll(senderSpecification, pageable);
    }

    public Sender findById(UUID id){
        Optional<Sender> sender = senderRepository.findById(id);

        if (sender.isEmpty()) {
            throw new NotFoundException(SENDER_TYPE, "id", id.toString());
        }

        return sender.get();
    }

    public Sender addSender(SenderRequestDto senderRequestDto) {
        Sender sender = new Sender();

        sender.setName(senderRequestDto.getName());
        sender.setEmail(senderRequestDto.getEmail());
        sender.setCreatedAt(System.currentTimeMillis());

        return senderRepository.save(sender);
    }

    public Sender updateSender(SenderUpdateDto senderUpdateDto) {
        Sender sender = findById(senderUpdateDto.getId());

        sender.setName(senderUpdateDto.getName());
        sender.setEmail(senderUpdateDto.getEmail());
        sender.setUpdatedAt(System.currentTimeMillis());

        return senderRepository.save(sender);
    }

    public void deleteSender(String id) {
        UUID senderId = UUID.fromString(id);

        senderRepository.deleteById(senderId);
    }

    public Boolean existsByEmail(String email) {
        return senderRepository.existsByEmail(email);
    }

    public boolean isUniqueForUpdate(String email, UUID id) {
        return !senderRepository.existsByEmailAndNotId(email, id);
    }

}
