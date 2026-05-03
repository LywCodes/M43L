package ita.service;

import ita.dto.*;
import ita.entity.CampaignApprovalHistory;
import ita.entity.CampaignHeader;
import ita.enumeration.CampaignApprovalStatus;
import ita.enumeration.CampaignStatus;
import ita.enumeration.CampaignType;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.repository.CampaignApprovalRepository;
import ita.repository.CampaignHeaderRepository;
import ita.specification.CampaignHistorySpecification;
import ita.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

import static ita.enumeration.EntityType.CAMPAIGN_HEADER_TYPE;

@Service
@Slf4j
@RequiredArgsConstructor
public class CampaignApprovalService {

    private final CampaignHeaderRepository campaignHeaderRepository;
    private final CampaignApprovalRepository historyRepository;
    private final UserService userService;
    private final ObjectProvider<CampaignHeaderService> headerServiceProvider;
    private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Transactional
    public void submitForApproval(CampaignHeader campaign) {
        saveHistory(campaign, CampaignApprovalStatus.REQUEST_SUBMITTED, "Campaign submitted for approval", campaign.getApproverId() );
    }

    @Transactional(rollbackFor = Exception.class)
    public void approve(UUID id, ApprovalRequestDto request) throws SchedulerException {
        CampaignHeader campaign = validateCampaignForApproval(id);

        if (campaign.getType() == CampaignType.IMMEDIATE){
            processApproval(campaign, request);

            log.info("Immediate Campaign {} approved and execution triggered ", id);
        } else {
            long currentTime = System.currentTimeMillis();

            if (currentTime > campaign.getScheduledTime()){
                campaign.setStatus(CampaignStatus.CANCELED);
                campaign.setFailedAt(currentTime);
                campaign.setFailureReason("Scheduled time has passed. Campaign automatically canceled.");

                campaignHeaderRepository.save(campaign);

                saveHistory(campaign, CampaignApprovalStatus.AUTO_CANCEL, "Campaign automatically canceled, scheduled time has passed", SYSTEM_USER_ID);
                return;
            }
            processApproval(campaign, request);
        }
    }

    @Transactional
    public void reject(UUID id, ApprovalRequestDto request) {

        CampaignHeader campaign = validateCampaignForApproval(id);

        campaign.setStatus(CampaignStatus.REJECTED);
        campaign.setRejectionReason(request.getReason());

        campaignHeaderRepository.save(campaign);
        saveHistory(campaign, CampaignApprovalStatus.REJECTED, request.getReason(), AuthUtil.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRequest(UUID id, ApprovalRequestDto request) {
        CampaignHeader campaign = campaignHeaderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CAMPAIGN_HEADER_TYPE, "id", id.toString()));

        UUID currentUserId = AuthUtil.getUserId();

        if (!campaign.getRequesterId().equals(currentUserId)) {
            throw new CustomException("action denied: you are not the requester from this campaign.");
        }

        if (campaign.getStatus() != CampaignStatus.WAITING_APPROVAL) {
            throw new CustomException("Cancel Failed: Campaign already be processed.");
        }

        campaign.setStatus(CampaignStatus.CANCELED);
        campaignHeaderRepository.save(campaign);

        saveHistory(campaign, CampaignApprovalStatus.REQUEST_CANCELLED, request.getReason(), currentUserId);

        log.info("Campaign request {} cancelled by requester {}", id, currentUserId);
    }


    public Page<ApprovalLogDto> getHistoryLogs(ApprovalHistorySearchCriteria criteria) {

        UUID currentUserId = AuthUtil.getUserId();
        int page = criteria.getPage();
        int size = (criteria.getSize() <= 0) ? 10 : criteria.getSize();
        int pageSize = (size == 999) ? Integer.MAX_VALUE : size;

        String sortParam = (criteria.getParam() == null) ? "createdAt" : criteria.getParam();
        String sortType = (criteria.getType() == null) ? "desc" : criteria.getType();

        Sort sort = sortType.equalsIgnoreCase("desc")
                ? Sort.by(sortParam).descending()
                : Sort.by(sortParam).ascending();

        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Specification<CampaignApprovalHistory> spec = Specification.where(
                CampaignHistorySpecification.belongsToRequester(currentUserId)
        ).and(
                Specification.allOf(
                        CampaignHistorySpecification.campaignNameLike(criteria.getCampaignName()),
                        CampaignHistorySpecification.statusEqual(criteria.getStatus())
                )
        );

        return historyRepository.findAll(spec, pageable)
                .map(log -> ApprovalLogDto.builder()
                        .campaignName(log.getCampaignName())
                        .status(log.getStatus().name())
                        .requesterName(log.getRequesterName())
                        .approverName(log.getApproverName())
                        .reason(log.getReason())
                        .timestamp(log.getCreatedAt())
                        .build());
    }

//    @Scheduled(cron = "0 */20 * * * *")
//    @Transactional
//    public void processAutoCancel() {
//        long now = System.currentTimeMillis();
//
//        List<CampaignHeader> expired = campaignHeaderRepository
//                .findExpiredScheduledCampaigns(now);
//
//        for (CampaignHeader campaignHeader : expired) {
//            campaignHeader.setStatus(CampaignStatus.CANCELED);
//            campaignHeader.setFailureReason("Campaign automatically canceled, approval timeout");
//
//            campaignHeaderRepository.save(campaignHeader);
//
//            saveHistory(campaignHeader, CampaignApprovalStatus.AUTO_CANCEL, "Scheduled time passed without approval(expired)", SYSTEM_USER_ID);
//            log.info("Scheduled time has passed. Campaign automatically canceled: {}", campaignHeader.getId());
//        }
//    }

    private void saveHistory(CampaignHeader campaignHeader, CampaignApprovalStatus status, String reason, UUID approverId) {

        String requesterUsername = userService.getUsernameById(campaignHeader.getRequesterId());
        String approverUsername = userService.getUsernameById(approverId);

        CampaignApprovalHistory history = CampaignApprovalHistory.builder()
                .campaignHeader(campaignHeader)
                .campaignName(campaignHeader.getName())
                .requesterId(campaignHeader.getRequesterId())
                .requesterName(requesterUsername)
                .approverId(approverId)
                .approverName(approverUsername)
                .status(status)
                .reason(reason)
                .createdAt(System.currentTimeMillis())
                .build();
        historyRepository.save(history);
    }

    private void processApproval (CampaignHeader campaign, ApprovalRequestDto request) throws SchedulerException {
        UUID currentUserId = AuthUtil.getUserId();

        campaign.setStatus(CampaignStatus.SCHEDULED);
        campaign.setApproverId(currentUserId);

        campaignHeaderRepository.save(campaign);
        saveHistory(campaign, CampaignApprovalStatus.APPROVED, request.getReason(), currentUserId);

        headerServiceProvider.getObject().initiateExecution(campaign);
    }

    private CampaignHeader validateCampaignForApproval(UUID id){
        CampaignHeader campaign = campaignHeaderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CAMPAIGN_HEADER_TYPE, "id", id.toString()));

        UUID currentUserId = AuthUtil.getUserId();

        if (!Objects.equals(campaign.getApproverId(), currentUserId)) {
            throw new CustomException("Unauthorized: You are not the assigned approver.");
        }

        if (campaign.getStatus() != CampaignStatus.WAITING_APPROVAL) {
            throw new CustomException("Only campaigns in WAITING_APPROVAL status can be processed");
        }

        return campaign;
    }

}
