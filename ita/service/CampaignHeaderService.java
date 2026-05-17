package ita.service;

import ita.aspect.GenerateServiceLog;
import ita.dto.*;
import ita.entity.*;
import ita.enumeration.CampaignStatus;
import ita.exception.CustomException;
import ita.exception.NotFoundException;
import ita.job.CampaignExecutionJob;
import ita.repository.CampaignHeaderRepository;
import ita.specification.CampaignHeaderSpecification;
import ita.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static ita.enumeration.CampaignStatus.*;
import static ita.enumeration.CampaignType.IMMEDIATE;
import static ita.enumeration.EntityType.CAMPAIGN_HEADER_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;

@Service
@Slf4j
public class CampaignHeaderService {

    private final CampaignHeaderRepository campaignHeaderRepository;
    private final CampaignDetailService campaignDetailService;
    private final SenderService senderService;
    private final ContentService contentService;
    private final AttachmentService attachmentService;
    private final ContactGroupService contactGroupService;
    private final Scheduler quartzScheduler;
    private final  ContentValidationService contentValidationService;
    private static final String CAMPAIGN_JOB = "campaign-jobs";
    private final CampaignApprovalService approvalService;
    private final UserService userService;

    public CampaignHeaderService(CampaignHeaderRepository campaignHeaderRepository,
                                 CampaignDetailService campaignDetailService,
                                 SenderService senderService,
                                 ContentService contentService,
                                 AttachmentService attachmentService,
                                 ContactGroupService contactGroupService,
                                 ContentValidationService contentValidationService,
                                 Scheduler quartzScheduler,
                                 CampaignApprovalService approvalService, UserService userService) {
        this.campaignHeaderRepository = campaignHeaderRepository;
        this.campaignDetailService = campaignDetailService;
        this.senderService = senderService;
        this.contentService = contentService;
        this.attachmentService = attachmentService;
        this.contactGroupService = contactGroupService;
        this.contentValidationService = contentValidationService;
        this.quartzScheduler = quartzScheduler;
        this.approvalService = approvalService;
        this.userService = userService;
    }

    @Transactional
    @GenerateServiceLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = ADD_OPERATION)
    public CampaignHeaderResponseDto createScheduledCampaign(CampaignHeaderRequestDto request) {

        Sender sender = senderService.findById(UUID.fromString(request.getSenderId()));
        if (!sender.isApproved()) {
            throw new CustomException("Sender is not approved");
        }

        Content content = contentService.findById(UUID.fromString(request.getContentId()));

        if (!content.isApproved()) {
            throw new CustomException("content is not approved");
        }

        ContactGroup contactGroup = contactGroupService.findById(UUID.fromString(request.getContactGroupId()));

        validateCampaignBeforeSending(content.getId(), contactGroup.getId());

        Attachment attachment = null;
        if (request.getAttachmentId() != null && !request.getAttachmentId().trim().isEmpty()) {
            attachment = attachmentService.findById(UUID.fromString(request.getAttachmentId()));

            if (!attachment.isApproved()) {
                throw new CustomException("attachment is not approved");
            }
        }

        CampaignStatus status = Boolean.TRUE.equals(request.getIsDraft()) ? DRAFT : WAITING_APPROVAL;
        UUID userId = AuthUtil.getUserId();

        if (userId.equals(request.getApproverId())) {
            throw new CustomException("You can't sign yourself as approver for this campaign");
        }

        CampaignHeader newCampaignHeader = CampaignHeader.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .type(request.getType())
                .sender(sender)
                .content(content)
                .attachment(attachment)
                .contactGroup(contactGroup)
                .scheduledTime(request.getScheduledTime())
                .requesterId(userId)
                .approverId(request.getApproverId())
                .status(status)
                .build();

        CampaignHeader campaignHeader = campaignHeaderRepository.save(newCampaignHeader);

        if (!Boolean.TRUE.equals(request.getIsDraft())) {
            approvalService.submitForApproval(campaignHeader);
        }

        return mapToResponseDto(campaignHeader);
    }

    @Transactional
    public CampaignHeaderResponseDto updateCampaignHeader(CampaignHeaderUpdateDto updateDto) throws MethodArgumentNotValidException, NoSuchMethodException {
        CampaignHeader existing = findById(updateDto.getId());
        if(existing.getStatus() != DRAFT){
            throw new CustomException("Cannot update campaign that has been submitted.");
        }

        if (!existing.getName().equals(updateDto.getName()) &&
                campaignHeaderRepository.existsByNameActiveAndIdNot(updateDto.getName(), updateDto.getId())) {

            Method method = this.getClass().getDeclaredMethod("updateCampaignHeader", CampaignHeaderUpdateDto.class);
            MethodParameter parameter = new MethodParameter(method, 0);

            BeanPropertyBindingResult result = new BeanPropertyBindingResult(updateDto, "name");

            result.addError(new FieldError("name",
                    "name",
                    "This name already used."));

            throw new MethodArgumentNotValidException(parameter, result);
        }

        UUID userId = AuthUtil.getUserId();

        if (userId.equals(updateDto.getApproverId())) {
            throw new CustomException("You can't sign yourself as approver for this campaign");
        }

        Sender sender = senderService.findById(UUID.fromString(updateDto.getSenderId()));

        if (!sender.isApproved()) {
            throw new CustomException("Sender is not approved");
        }

        Content content = contentService.findById(UUID.fromString(updateDto.getContentId()));

        if (!content.isApproved()) {
            throw new CustomException("content is not approved");
        }

        ContactGroup contactGroup = contactGroupService.findById(UUID.fromString(updateDto.getContactGroupId()));

        validateCampaignBeforeSending(content.getId(), contactGroup.getId());

        Attachment attachment = null;
        if (updateDto.getAttachmentId() != null && !updateDto.getAttachmentId().trim().isEmpty()) {
            attachment = attachmentService.findById(UUID.fromString(updateDto.getAttachmentId()));

            if (!content.isApproved()) {
                throw new CustomException("content is not approved");
            }
        }

        existing.setName(updateDto.getName());
        existing.setSubject(updateDto.getSubject());
        existing.setSender(sender);
        existing.setContent(content);
        existing.setAttachment(attachment);
        existing.setContactGroup(contactGroup);
        existing.setScheduledTime(updateDto.getScheduledTime());
        existing.setType(updateDto.getType());
        existing.setApproverId(updateDto.getApproverId());

        if (!Boolean.TRUE.equals(updateDto.getIsDraft())) {
            existing.setStatus(WAITING_APPROVAL);
            approvalService.submitForApproval(existing);
        }

        CampaignHeader updated = campaignHeaderRepository.save(existing);

        return mapToResponseDto(updated);
    }

    public CampaignHeaderResponseDto mapToResponseDto(CampaignHeader entity) {
        return CampaignHeaderResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subject(entity.getSubject())
                .type(entity.getType())
                .status(entity.getStatus())
                .senderId(entity.getSender() != null ? entity.getSender().getId() : null)
                .contentId(entity.getContent() != null ? entity.getContent().getId() : null)
                .attachmentId(entity.getAttachment() != null ? entity.getAttachment().getId() : null)
                .contactGroupId(entity.getContactGroup() != null ? entity.getContactGroup().getId() : null)
                .scheduledTime(entity.getScheduledTime())
                .requesterId(entity.getRequesterId())
                .approverId(entity.getApproverId())
                .rejectionReason(entity.getRejectionReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private CampaignApprovalResponseDto mapToApprovalResponseDto(CampaignHeader campaignHeader) {
        LocalUser requester = userService.findById(campaignHeader.getRequesterId());
        LocalUser approver = userService.findById(campaignHeader.getApproverId());

        return CampaignApprovalResponseDto.builder()
                .id(campaignHeader.getId())
                .name(campaignHeader.getName())
                .type(campaignHeader.getType())
                .status(campaignHeader.getStatus())
                .senderId(campaignHeader.getSender().getId())
                .senderName(campaignHeader.getSender().getName())
                .contentId(campaignHeader.getContent().getId())
                .contentName(campaignHeader.getContent().getName())
                .attachmentId(campaignHeader.getAttachment() != null ? campaignHeader.getAttachment().getId() : null)
                .attachmentName(campaignHeader.getAttachment() != null ? campaignHeader.getAttachment().getName() : null)
                .contactGroupId(campaignHeader.getContactGroup().getId())
                .contactGroupName(campaignHeader.getContactGroup().getName())
                .scheduledTime(campaignHeader.getScheduledTime())
                .requesterId(requester.getId())
                .requesterName(requester.getName())
                .approverId(approver.getId())
                .approverName(approver.getName())
                .rejectionReason(campaignHeader.getRejectionReason())
                .build();
    }

    public CampaignHeaderResponseDto findDtoById(UUID id) {
        return mapToResponseDto(findById(id));
    }

    public void initiateExecution(CampaignHeader campaign) throws SchedulerException {
        if (campaign.getType() == IMMEDIATE) {
            executeImmediateCampaign(campaign);
        } else {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            scheduleQuartzJob(campaign);
                        } catch (SchedulerException e) {
                            log.error("quartz  scheduling failed", e);
                        }
                    }
                });
            } else {
                scheduleQuartzJob(campaign);
            }

        }
    }

    public List<CampaignHeaderResponseDto>getPendingApprovals(ApprovalSearchCriteria searchCriteria){
        UUID currentUserId = AuthUtil.getUserId();

        List<CampaignHeader>pendingCampaigns = campaignHeaderRepository.findPendingApprovals(currentUserId);

        return pendingCampaigns.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public Page<CampaignApprovalResponseDto> getAllApproval(ApprovalSearchCriteria searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = searchCriteria.getType().equalsIgnoreCase("desc")
                ? Sort.by(searchCriteria.getParam()).descending()
                : Sort.by(searchCriteria.getParam()).ascending();

        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize, sort);

        UUID currentUserId = AuthUtil.getUserId();

        Specification<CampaignHeader> campaignHeaderSpecification = Specification.allOf(
                CampaignHeaderSpecification.statusEqual(searchCriteria.getStatus())
                        .and(CampaignHeaderSpecification.typeEqual(searchCriteria.getCampaignType()))
                        .and(CampaignHeaderSpecification.nameLike(searchCriteria.getName()))
                        .and(CampaignHeaderSpecification.approver(currentUserId).or(CampaignHeaderSpecification.requester(currentUserId)))
        );

        Page<CampaignHeader> campaignHeaders = campaignHeaderRepository.findAll(campaignHeaderSpecification, pageable);

        List<CampaignApprovalResponseDto> campaignApprovalResponseDtos = campaignHeaders.stream().map(this::mapToApprovalResponseDto).toList();

        return new PageImpl<>(campaignApprovalResponseDtos, pageable, campaignHeaders.getTotalElements());
    }

    public void executeCampaign(String campaignId) {
        UUID id = UUID.fromString(campaignId);
        CampaignHeader campaignHeader = findById(id);

        validateCampaignBeforeSending(campaignHeader.getContent().getId(),campaignHeader.getContactGroup().getId());

        if (campaignHeader.getStatus() != SCHEDULED) {
            log.warn("Campaign {} is not in SCHEDULED status, current status: {}", campaignId, campaignHeader.getStatus());
            return;
        }

        campaignHeader.setStatus(CampaignStatus.IN_PROGRESS);
        campaignHeader.setStartedAt(System.currentTimeMillis());

        CampaignHeader createdCampaignHeader = campaignHeaderRepository.save(campaignHeader);

        campaignDetailService.createCampaign(createdCampaignHeader);

        createdCampaignHeader.setStatus(SENT);
        createdCampaignHeader.setCompletedAt(System.currentTimeMillis());

        campaignHeaderRepository.save(createdCampaignHeader);
    }


    private void executeImmediateCampaign(CampaignHeader campaignHeader) {
        executeCampaign(campaignHeader.getId().toString());
    }


    public Page<CampaignHeader> getAllCampaigns(CampaignHeaderSearchCriteria searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = searchCriteria.getType().equalsIgnoreCase("desc")
                ? Sort.by(searchCriteria.getParam()).descending()
                : Sort.by(searchCriteria.getParam()).ascending();

        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize, sort);

        Specification<CampaignHeader> campaignHeaderSpecification = Specification.allOf(
                CampaignHeaderSpecification.statusEqual(searchCriteria.getStatus())
                        .and(CampaignHeaderSpecification.typeEqual(searchCriteria.getCampaignType()))
                        .and(CampaignHeaderSpecification.nameLike(searchCriteria.getName()))
                        .and(CampaignHeaderSpecification.scheduledTime(searchCriteria.getScheduledTime()))
        );

        return campaignHeaderRepository.findAll(campaignHeaderSpecification, pageable);
    }

    public CampaignHeader findById(UUID id) {
        return campaignHeaderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CAMPAIGN_HEADER_TYPE, "id", id.toString()));
    }

    public void deleteCampaign(String campaignId) throws SchedulerException {
        UUID id = UUID.fromString(campaignId);
        CampaignHeader campaignHeader = findById(id);


        if (campaignHeader.getQuartzJobId() != null && campaignHeader.getStatus() == SCHEDULED) {
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), CAMPAIGN_JOB);
            quartzScheduler.deleteJob(jobKey);
        }

        campaignHeaderRepository.deleteById(id);
    }

    public CampaignHeader cancelScheduledCampaign(String campaignId) throws SchedulerException {
        UUID id = UUID.fromString(campaignId);
        CampaignHeader campaignHeader = findById(id);

        if (campaignHeader.getStatus() != SCHEDULED) {
            throw new CustomException("Campaign is not in scheduled status");
        }

        if (campaignHeader.getQuartzJobId() != null) {
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), CAMPAIGN_JOB);
            quartzScheduler.deleteJob(jobKey);
        }

        campaignHeader.setStatus(CampaignStatus.FAILED);
        campaignHeader.setFailedAt(System.currentTimeMillis());
        campaignHeader.setFailureReason("Cancelled by user");

        return campaignHeaderRepository.save(campaignHeader);
    }

    public CampaignHeader rescheduleCampaign(RescheduleCampaignDto rescheduleCampaignDto) throws SchedulerException {
        CampaignHeader campaignHeader = findById(rescheduleCampaignDto.getCampaignId());

        if (campaignHeader.getStatus() != SCHEDULED) {
            throw new CustomException("Campaign is not in scheduled status");
        }

        if (campaignHeader.getQuartzJobId() != null) {
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), CAMPAIGN_JOB);
            quartzScheduler.deleteJob(jobKey);

//            Trigger trigger = TriggerBuilder.newTrigger().build();
//            quartzScheduler.rescheduleJob(TriggerKey.triggerKey(jobKey.getName(), jobKey.getGroup()), trigger);
        }

        campaignHeader.setScheduledTime(rescheduleCampaignDto.getScheduledTime());
        scheduleQuartzJob(campaignHeader);

        return campaignHeaderRepository.save(campaignHeader);
    }

    public List<CampaignHeader> getScheduledCampaignsToExecute() {
        Specification<CampaignHeader> campaignHeaderSpecification = CampaignHeaderSpecification.statusEqual(SCHEDULED)
                .and(CampaignHeaderSpecification.scheduledTime(System.currentTimeMillis()));

        return campaignHeaderRepository.findAll(campaignHeaderSpecification);
    }

    public Boolean existsByNameActive(String name) {
        return campaignHeaderRepository.existsByNameActive(name);
    }

    public Boolean existsByNameActiveAndIdNot(String name, UUID id) {
        return campaignHeaderRepository.existsByNameActiveAndIdNot(name, id);
    }

    public void scheduleQuartzJob(CampaignHeader campaignHeader) throws SchedulerException {
        String jobId = "campaign-job-" + campaignHeader.getId().toString();

        JobKey jobKey = JobKey.jobKey(jobId, CAMPAIGN_JOB);

        if (quartzScheduler.checkExists(jobKey)) {
            log.info("job quartz with id {} already exist, removing old job", jobId);
            quartzScheduler.deleteJob(jobKey);
        }

        JobDetail jobDetail = JobBuilder.newJob(CampaignExecutionJob.class)
                .withIdentity(jobId, CAMPAIGN_JOB)
                .usingJobData("campaignId", campaignHeader.getId().toString())
                .build();

        Date scheduledDate = new Date(campaignHeader.getScheduledTime());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("campaign-trigger-" + campaignHeader.getId(), "campaign-triggers")
                .startAt(scheduledDate)
                .build();

        quartzScheduler.scheduleJob(jobDetail, trigger);

        campaignHeader.setQuartzJobId(jobId);
        campaignHeader.setScheduledAt(scheduledDate.getTime());

        campaignHeaderRepository.save(campaignHeader);
    }

    private void validateCampaignBeforeSending(UUID contentId, UUID groupId){

        var validationResult = contentValidationService.validateContentCompatibility(contentId, groupId);

        if (!validationResult.isValid()) {
            String missingAttributes = String.join(", ", validationResult.getMissingAttributes());

            throw new CustomException(
                    String.format("Template validation failed, missing attributes in group: [%s]", missingAttributes)
            );
        }
    }

}