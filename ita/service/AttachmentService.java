package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.*;
import ita.entity.Attachment;
import ita.enumeration.ApprovalStatus;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.repository.AttachmentRepository;
import ita.specification.AttachmentSpecification;
import ita.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;
import static ita.enumeration.EntityType.SENDER_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;


@Service
@Slf4j
public class AttachmentService {

    private final String serviceName;
    private final Tracer tracer;
    private final AttachmentRepository attachmentRepository;
    private final ApprovalAuditService approvalAuditService;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             @Value("${spring.application.name}") String serviceName,
                             Tracer tracer,
                             ApprovalAuditService approvalAuditService) {
        this.attachmentRepository = attachmentRepository;
        this.serviceName = serviceName;
        this.tracer = tracer;
        this.approvalAuditService = approvalAuditService;
    }

    public Page<Attachment> findByFilter(BaseSearchCriteriaDto searchCriteria, ApprovalStatus filterStatus) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();
        String sortParam = searchCriteria.getParam();

        Sort sort = searchCriteria.getType().equalsIgnoreCase("desc") ? Sort.by(sortParam).descending() : Sort.by(sortParam).ascending();
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize, sort);

        Specification<Attachment> spec = Specification.allOf(
                AttachmentSpecification.nameLike(searchCriteria.getName()),
                AttachmentSpecification.hasStatus(filterStatus)
        );

        return attachmentRepository.findAll(spec, pageable);
       // return attachmentRepository.findProjection(pageable, searchCriteria.getName());
    }

    public AttachmentResponseDto findDtoById(UUID id) {
        Attachment attachment = findById(id);

        AttachmentResponseDto attachmentResponseDto = new AttachmentResponseDto();

        attachmentResponseDto.setId(attachment.getId());
        attachmentResponseDto.setName(attachment.getName());

        return attachmentResponseDto;
    }

    @Transactional
    public void uploadAttachment(AttachmentRequestDto attachmentRequestDto) throws IOException, NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod("uploadAttachment", AttachmentRequestDto.class);

        if (attachmentRepository.existsByNameActive(attachmentRequestDto.getName())) {
            throw new CustomException("Name already used");
        }

        Attachment attachment = new Attachment();

        attachment.setName(attachmentRequestDto.getName());
        attachment.setFile(attachmentRequestDto.getFile().getBytes());

        MDC.put("entity_type", ATTACHMENT_TYPE.getValue());
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

        Attachment savedAttachment = attachmentRepository.save(attachment);

        String makerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(ATTACHMENT_TYPE)
                .entityId(attachment.getId())
                .entityIdentifier(savedAttachment.getName())
                .requesterEmail(makerEmail)
                .build();

        approvalAuditService.logRequest(logDto);

    }

    public void updateAttachment(AttachmentUpdateDto attachmentUpdateDto) {
        Attachment attachment = findById(attachmentUpdateDto.getId());

        attachment.setName(attachmentUpdateDto.getName());

        attachmentRepository.save(attachment);
    }

    public ByteArrayResource downloadAttachment(UUID id) {
        Attachment attachment = findById(id);

        return new ByteArrayResource(attachment.getFile());
    }

    public void deleteAttachment(UUID id) {
        attachmentRepository.deleteById(id);
    }

    public Attachment findById(UUID id) {
        Optional<Attachment> attachment = attachmentRepository.findById(id);

        if (attachment.isEmpty()) {
            throw new NotFoundException(ATTACHMENT_TYPE, "id", id.toString());
        }

        return attachment.get();
    }

    @Transactional
    public Attachment processAttachment(UUID id, ApprovalRequestDto request) {
        Attachment attachment = findById(id);

        if (!attachment.isPending()) {
            throw new CustomException("only attachment in pending status can be processed");
        }

        String attachmentChecker =  SecurityContextHolder.getContext().getAuthentication().getName();

        if (request.getIsApproved()){
            attachment.approve(attachmentChecker);
        } else {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new CustomException("reason is required");
            } attachment.reject(attachmentChecker, request.getReason());
        }

        Attachment updatedAttachment = attachmentRepository.save(attachment);
        String checkerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String makerEmail = updatedAttachment.getCreatedBy();

        ApprovalStatus status = request.getIsApproved()
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.REJECTED;

        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(SENDER_TYPE)
                .entityId(updatedAttachment.getId())
                .entityIdentifier(updatedAttachment.getName())
                .requesterEmail(makerEmail)
                .approverEmail(checkerEmail)
                .status(status)
                .reason(request.getReason())
                .build();

        approvalAuditService.logDecision(logDto);

        return updatedAttachment;
    }

    public Boolean existsByNameActive(String name) {
        return attachmentRepository.existsByNameActive(name);
    }

    public boolean isUniqueForUpdateActive(String name, UUID id) {
        return !attachmentRepository.existsByNameActiveAndIdNot(name, id);
    }
}