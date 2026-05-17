package ita.service;

import ita.dto.ApprovalLogSearchCriteriaDto;
import ita.dto.GeneralApprovalLogDto;
import ita.entity.ApprovalAuditLog;
import ita.enumeration.ApprovalStatus;
import ita.repository.ApprovalAuditLogRepository;
import ita.specification.ApprovalLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApprovalAuditService {
    private final ApprovalAuditLogRepository approvalAuditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void logRequest(GeneralApprovalLogDto logDto) {
        ApprovalAuditLog log = new ApprovalAuditLog();

        log.setEntityType(logDto.getEntityType());
        log.setEntityId(logDto.getEntityId());
        log.setEntityIdentifier(logDto.getEntityIdentifier());

        log.setRequesterEmail(logDto.getRequesterEmail());
        log.setApproverEmail(null);

        log.setStatus(ApprovalStatus.PENDING);
        log.setReason(null);

        approvalAuditLogRepository.save(log);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void logDecision(GeneralApprovalLogDto logDto) {
        ApprovalAuditLog log = new ApprovalAuditLog();

        log.setEntityType(logDto.getEntityType());
        log.setEntityId(logDto.getEntityId());
        log.setEntityIdentifier(logDto.getEntityIdentifier());

        log.setRequesterEmail(logDto.getRequesterEmail());
        log.setApproverEmail(logDto.getApproverEmail());

        log.setStatus(logDto.getStatus());
        log.setReason(logDto.getReason());

        approvalAuditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ApprovalAuditLog> findAll(ApprovalLogSearchCriteriaDto criteria) {
        int pageSize = (criteria.getSize() == 999)
                ? Integer.MAX_VALUE : criteria.getSize();

        Sort sort = Sort.by(criteria.getParam());
        Pageable pageable = PageRequest.of(
                criteria.getPage(), pageSize,
                "desc".equalsIgnoreCase(criteria.getType()) ? sort.descending() : sort.ascending()
        );

        Specification<ApprovalAuditLog> spec = Specification.allOf(
                ApprovalLogSpecification.entityTypeEqual(criteria.getEntityType()),
                ApprovalLogSpecification.entityIdentifierLike(criteria.getName()),
                ApprovalLogSpecification.statusEqual(criteria.getStatus()),
                ApprovalLogSpecification.requesterEmailLike(criteria.getRequesterEmail()),
                ApprovalLogSpecification.approverEmailLike(criteria.getApproverEmail())
        );

        return approvalAuditLogRepository.findAll(spec, pageable);

    }

}
