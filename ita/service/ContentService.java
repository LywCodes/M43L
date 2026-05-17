package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.ApprovalRequestDto;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContentRequestDto;
import ita.dto.ContentResponseDto;
import ita.dto.*;
import ita.entity.Content;
import ita.enumeration.ApprovalStatus;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.projection.BaseAutoIncrementIdProjection;
import ita.projection.ContentProjection;
import ita.repository.ContentRepository;
import ita.util.AuthUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static ita.enumeration.EntityType.CONTENT_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;

@Service
@Slf4j
public class ContentService {

    private final String serviceName;
    private final Tracer tracer;

    private final ContentRepository contentRepository;
    private final ApprovalAuditService approvalAuditService;

    @PersistenceContext
    private EntityManager entityManager;

    public ContentService(ContentRepository contentRepository,
                          Tracer tracer,
                          @Value("${spring.application.name}") String serviceName,
                          ApprovalAuditService approvalAuditService) {
        this.contentRepository = contentRepository;
        this.tracer = tracer;
        this.serviceName = serviceName;
        this.approvalAuditService = approvalAuditService;
    }

    public Page<ContentProjection> findAllContent(BaseSearchCriteriaDto searchCriteria,
                                                  ApprovalStatus filterStatus) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending());

        Page<ContentProjection> contentProjections = contentRepository.findProjection(pageable, searchCriteria.getName(), filterStatus);

        List<ContentProjection> contentProjectionList = contentProjections.stream().map(this::mapToDecodedHtmlProjection).toList();

        return new PageImpl<>(contentProjectionList, pageable, contentProjections.getTotalElements());
    }

    public Page<BaseAutoIncrementIdProjection> findBaseProjection(BaseSearchCriteriaDto searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending());

        Page<BaseAutoIncrementIdProjection> contentProjections = contentRepository.findBaseProjection(pageable, searchCriteria.getName());

        List<BaseAutoIncrementIdProjection> contentProjectionList = contentProjections.stream().map(content ->
            BaseAutoIncrementIdProjection.builder()
                    .id(content.getId())
                    .name(content.getName())
                    .autoIncrementId(content.getAutoIncrementId())
                    .html(decodeBase64(content.getHtml()))
                    .build()
        ).toList();

        return new PageImpl<>(contentProjectionList, pageable, contentProjections.getTotalElements());
    }

    public Content findById(UUID id) {
        Optional<Content> content = contentRepository.findById(id);

        if (content.isEmpty()) {
            throw new NotFoundException(CONTENT_TYPE, "id", id.toString());
        }

        return content.get();
    }

    public ContentResponseDto findDecodedHtmlById(UUID id) {
        Optional<Content> content = contentRepository.findById(id);

        if (content.isEmpty()) {
            throw new NotFoundException(CONTENT_TYPE, "id", id.toString());
        }

        Content currentContent = content.get();

        return ContentResponseDto.builder()
                .id(currentContent.getId())
                .name(currentContent.getName())
                .html(decodeBase64(currentContent.getHtml()))
                .build();
    }

    @Transactional
    public Content addContent(ContentRequestDto contentRequestDto) {

        if (contentRepository.existByNameActive(contentRequestDto.getName())) {
            throw new CustomException("Name already used");
        }

        Content content = new Content();

        content.setName(contentRequestDto.getName());
        content.setHtml(contentRequestDto.getHtml());
        content.setNumberOfParam(contentRequestDto.getNumberOfParam());

        String methodName = StackWalker.getInstance()
                .walk(frames -> frames.findFirst()
                        .map(StackWalker.StackFrame::getMethodName))
                .orElse("unknown_method");

        MDC.put("entity_type", CONTENT_TYPE.getValue());
        MDC.put("operation_type", ADD_OPERATION.getValue());
        MDC.put("method", methodName);
        MDC.put("service", serviceName);
        MDC.put("trace_id", Objects.requireNonNull(tracer.currentTraceContext().context()).traceId());

        MDC.remove("traceId");
        MDC.remove("spanId");

        String name = AuthUtil.getUsername();
        Class<?> clazz = this.getClass();
        String simpleName = clazz.getSimpleName();

        MDC.put("user", name);
        MDC.put("actual_class", simpleName);

        entityManager.persist(content);
        entityManager.flush();
        entityManager.refresh(content);

        Content savedContent = contentRepository.save(content);

        String makerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(CONTENT_TYPE)
                .entityId(savedContent.getId())
                .entityIdentifier(savedContent.getName())
                .requesterEmail(makerEmail)
                .build();

        approvalAuditService.logRequest(logDto);

        return savedContent;

    }

    @Transactional
    public Content processApproval(UUID ID, ApprovalRequestDto request){
        Content content = findById(ID);

        if (!content.isPending()) {
            throw new CustomException("only content in pending status can be processed");
        }

        String contentChecker = SecurityContextHolder.getContext().getAuthentication().getName();

        if (request.getIsApproved()){
            content.approve(contentChecker);
        } else {
            if (request.getReason() == null || request.getReason().isBlank()) {
                throw new CustomException("reason is required");
            } content.reject(contentChecker,  request.getReason());
        }

        Content updatedContent = contentRepository.save(content);

        String checkerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String makerEmail = updatedContent.getCreatedBy();

        ApprovalStatus status = request.getIsApproved()
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.REJECTED;

        GeneralApprovalLogDto logDto = GeneralApprovalLogDto.builder()
                .entityType(CONTENT_TYPE)
                .entityId(updatedContent.getId())
                .entityIdentifier(updatedContent.getName())
                .requesterEmail(makerEmail)
                .approverEmail(checkerEmail)
                .status(status)
                .reason(request.getReason())
                .build();

        approvalAuditService.logDecision(logDto);

        return updatedContent;
    }

    public boolean existByNameActive(String name) {
        return contentRepository.existByNameActive(name);
    }

    public void deleteContent(String id) {
        UUID contentId = UUID.fromString(id);

        contentRepository.deleteById(contentId);
    }

    private String decodeBase64(String base64String) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private ContentProjection mapToDecodedHtmlProjection(ContentProjection contentProjection) {
        return ContentProjection.builder()
                .id(contentProjection.getId())
                .autoIncrementId(contentProjection.getAutoIncrementId())
                .name(contentProjection.getName())
                .html(decodeBase64(contentProjection.getHtml()))
                .createdBy(contentProjection.getCreatedBy())
                .createdAt(contentProjection.getCreatedAt())
                .updatedBy(contentProjection.getUpdatedBy())
                .updatedAt(contentProjection.getUpdatedAt())
                .approvedBy(contentProjection.getApprovedBy())
                .approvedAt(contentProjection.getApprovedAt())
                .approvalStatus(contentProjection.getApprovalStatus())
                .rejectedBy(contentProjection.getRejectedBy())
                .rejectedAt(contentProjection.getRejectedAt())
                .rejectionReason(contentProjection.getRejectionReason())
                .build();
    }

}
