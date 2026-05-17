package ita.service;

import ita.dto.*;
import ita.entity.Sender;
import ita.enumeration.ApprovalStatus;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.repository.SenderRepository;
import ita.specification.SenderSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.SENDER_TYPE;

@Slf4j
@Service
public class SenderService {

    private final SenderRepository senderRepository;
    private final ApprovalAuditService approvalAuditService;

    private static final List<String> properties = List.of("id", "name", "approval_status", "created_by", "created_at", "updated_by", "updated_at", "approved_by", "approved_at",
            "rejected_by", "rejected_at", "rejection_reason");

    public SenderService(SenderRepository senderRepository,
                         ApprovalAuditService approvalAuditService
    )
                         {
        this.senderRepository = senderRepository;
        this.approvalAuditService = approvalAuditService;
    }

    public Page<Sender> findAllSender(BaseSearchCriteriaDto searchCriteriaDto, ApprovalStatus filterStaus) {
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
                        .and(SenderSpecification.hasStatus(filterStaus))
        );

        return senderRepository.findAll(senderSpecification, pageable);
    }

    public Page<SenderResponseDto> findProjection(BaseSearchCriteriaDto searchCriteriaDto) {
        int pageSize = (searchCriteriaDto.getSize() == 999) ? Integer.MAX_VALUE : searchCriteriaDto.getSize();
        Sort sort = Sort.by(searchCriteriaDto.getParam());
        Pageable pageable = PageRequest.of(
                searchCriteriaDto.getPage(),
                pageSize,
                "desc".equalsIgnoreCase(searchCriteriaDto.getType()) ? sort.descending() : sort.ascending()
        );

        return senderRepository.findProjection(pageable, searchCriteriaDto.getName());
    }

    public Sender findById(UUID id){
        Optional<Sender> sender = senderRepository.findById(id);

        if (sender.isEmpty()) {
            throw new NotFoundException(SENDER_TYPE, "id", id.toString());
        }

        return sender.get();
    }

    @Transactional
    public Sender addSender(SenderRequestDto senderRequestDto) {
        if (senderRepository.existsByEmailActive(senderRequestDto.getEmail())) {
            throw new CustomException("Email already active");
        }

        Sender sender = new Sender();

        sender.setName(senderRequestDto.getName());
        sender.setEmail(senderRequestDto.getEmail());

        Sender savedSender = senderRepository.save(sender);

        String makerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(SENDER_TYPE)
                .entityId(savedSender.getId())
                .entityIdentifier(savedSender.getEmail())
                .requesterEmail(makerEmail)
                .build();

        approvalAuditService.logRequest(logDto);

        return savedSender;
    }

    public Sender updateSender(SenderUpdateDto senderUpdateDto) {
        Sender sender = findById(senderUpdateDto.getId());

        sender.setName(senderUpdateDto.getName());
        sender.setEmail(senderUpdateDto.getEmail());

        return senderRepository.save(sender);
    }

    public void deleteSender(String id) {
        UUID senderId = UUID.fromString(id);

        senderRepository.deleteById(senderId);
    }

    @Transactional
    public Sender processApproval(UUID id, ApprovalRequestDto request) {
        Sender sender = findById(id);

        if (!sender.isPending()) {
            throw new CustomException("only sender with pending status can be process");
        }

        String userChecker = SecurityContextHolder.getContext().getAuthentication().getName();

//        String userCheckerFromUtil = AuthUtil.getUsername();

        if (request.getIsApproved()){
            sender.approve(userChecker);
        } else  {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new CustomException("reason is required");
            } sender.reject(userChecker, request.getReason());
        }

        Sender updatedSender = senderRepository.save(sender);

        // LOG APPEND
        String checkerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String makerEmail = updatedSender.getCreatedBy();

        ApprovalStatus status = request.getIsApproved()
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.REJECTED;

        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(SENDER_TYPE)
                .entityId(updatedSender.getId())
                .entityIdentifier(updatedSender.getEmail())
                .requesterEmail(makerEmail)
                .approverEmail(checkerEmail)
                .status(status)
                .reason(request.getReason())
                .build();

        approvalAuditService.logDecision(logDto);

        return updatedSender;
    }

    public boolean existsByEmailActive(String email) {
        return  senderRepository.existsByEmailActive(email);
    }
    public boolean isUniqueForUpdateActive(String email, UUID id) {
        return !senderRepository.existsByEmailActiveAndIdNot(email, id);
    }

}
