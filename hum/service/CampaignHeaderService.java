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
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    @Autowired
    public CampaignHeaderService(CampaignHeaderRepository campaignHeaderRepository,
                                 CampaignDetailService campaignDetailService,
                                 SenderService senderService,
                                 ContentService contentService,
                                 AttachmentService attachmentService,
                                 ContactGroupService contactGroupService,
                                 Scheduler quartzScheduler) {
        this.campaignHeaderRepository = campaignHeaderRepository;
        this.campaignDetailService = campaignDetailService;
        this.senderService = senderService;
        this.contentService = contentService;
        this.attachmentService = attachmentService;
        this.contactGroupService = contactGroupService;
        this.quartzScheduler = quartzScheduler;
    }

    @GenerateServiceLog(entityType = CAMPAIGN_HEADER_TYPE, operationType = ADD_OPERATION)
    public CampaignHeader createScheduledCampaign(CampaignHeaderRequestDto request) throws SchedulerException {
        Sender sender = senderService.findById(UUID.fromString(request.getSenderId()));
        Content content = contentService.findById(UUID.fromString(request.getContentId()));
        ContactGroup contactGroup = contactGroupService.findById(UUID.fromString(request.getContactGroupId()));

        Attachment attachment = null;
        if (request.getAttachmentId() != null && !request.getAttachmentId().trim().isEmpty()) {
            attachment = attachmentService.findById(UUID.fromString(request.getAttachmentId()));
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
                .status(request.getIsDraft() ? DRAFT : SCHEDULED)
                .build();

        CampaignHeader campaignHeader = campaignHeaderRepository.save(newCampaignHeader);

        if (request.getType() == IMMEDIATE) {
            executeImmediateCampaign(campaignHeader);
        } else if (request.getIsDraft()) {
            return campaignHeader;
        }else {
            scheduleQuartzJob(campaignHeader);
        }

        return campaignHeader;
    }

    public CampaignHeader updateCampaignHeader(CampaignHeaderUpdateDto campaignHeaderUpdateDto) throws SchedulerException, MethodArgumentNotValidException, NoSuchMethodException {
        Sender sender = senderService.findById(UUID.fromString(campaignHeaderUpdateDto.getSenderId()));
        Content content = contentService.findById(UUID.fromString(campaignHeaderUpdateDto.getContentId()));
        ContactGroup contactGroup = contactGroupService.findById(UUID.fromString(campaignHeaderUpdateDto.getContactGroupId()));

        Attachment attachment = null;
        if (campaignHeaderUpdateDto.getAttachmentId() != null && !campaignHeaderUpdateDto.getAttachmentId().trim().isEmpty()) {
            attachment = attachmentService.findById(UUID.fromString(campaignHeaderUpdateDto.getAttachmentId()));
        }

        CampaignHeader campaignHeaderFromDB = findById(campaignHeaderUpdateDto.getId());

        if (!campaignHeaderFromDB.getName().equals(campaignHeaderUpdateDto.getName()) && existsByName(campaignHeaderUpdateDto.getName())) {
            Method method = this.getClass().getDeclaredMethod("updateCampaignHeader", CampaignHeaderUpdateDto.class);
            MethodParameter parameter = new MethodParameter(method, 0);

            BeanPropertyBindingResult result = new BeanPropertyBindingResult(campaignHeaderUpdateDto, "name");

            result.addError(new FieldError("name",
                    "name",
                    "This name already used."));

            throw new MethodArgumentNotValidException(parameter, result);
        }

        CampaignHeader newCampaignHeader = CampaignHeader.builder()
                .id(campaignHeaderUpdateDto.getId())
                .name(campaignHeaderUpdateDto.getName())
                .subject(campaignHeaderUpdateDto.getSubject())
                .type(campaignHeaderUpdateDto.getType())
                .sender(sender)
                .content(content)
                .attachment(attachment)
                .contactGroup(contactGroup)
                .scheduledTime(campaignHeaderUpdateDto.getScheduledTime())
                .status(campaignHeaderUpdateDto.getIsDraft() ? DRAFT : SCHEDULED)
                .build();

        CampaignHeader campaignHeader = campaignHeaderRepository.save(newCampaignHeader);

        if (campaignHeaderUpdateDto.getType().equals(IMMEDIATE)) {
            executeImmediateCampaign(campaignHeader);
        } else if (campaignHeaderUpdateDto.getIsDraft()) {
            return campaignHeader;
        } else {
            scheduleQuartzJob(campaignHeader);
        }

        return campaignHeader;
    }

    public void executeCampaign(String campaignId) {
        UUID id = UUID.fromString(campaignId);
        CampaignHeader campaignHeader = findById(id);

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

        Specification<CampaignHeader> campaignHeaderSpecification = Specification.where(CampaignHeaderSpecification.statusEqual(searchCriteria.getStatus()))
                .and(CampaignHeaderSpecification.typeEqual(searchCriteria.getCampaignType()))
                .and(CampaignHeaderSpecification.nameLike(searchCriteria.getName()))
                .and(CampaignHeaderSpecification.scheduledTime(searchCriteria.getScheduledTime()));

        return campaignHeaderRepository.findAll(campaignHeaderSpecification, pageable);
    }

    public CampaignHeader findById(UUID id) {
        return campaignHeaderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CAMPAIGN_HEADER_TYPE, "id", id.toString()));
    }

    public CampaignHeaderResponseDto findDtoById(UUID id) {
        CampaignHeader campaignHeader = findById(id);

        return CampaignHeaderResponseDto.builder()
                .id(campaignHeader.getId())
                .name(campaignHeader.getName())
                .subject(campaignHeader.getSubject())
                .type(campaignHeader.getType())
                .status(campaignHeader.getStatus())
                .senderId(campaignHeader.getSender().getId())
                .contentId(campaignHeader.getContent().getId())
                .attachmentId(campaignHeader.getAttachment() != null ? campaignHeader.getAttachment().getId() : null)
                .contactGroupId(campaignHeader.getContactGroup().getId())
                .scheduledTime(campaignHeader.getScheduledTime())
                .build();
    }

    public void deleteCampaign(String campaignId) throws SchedulerException {
        UUID id = UUID.fromString(campaignId);
        CampaignHeader campaignHeader = findById(id);

        if (campaignHeader.getQuartzJobId() != null && campaignHeader.getStatus() == SCHEDULED) {
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), "campaign-jobs");
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
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), "campaign-jobs");
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
            JobKey jobKey = JobKey.jobKey(campaignHeader.getQuartzJobId(), "campaign-jobs");
            quartzScheduler.deleteJob(jobKey);
        }

        campaignHeader.setScheduledTime(rescheduleCampaignDto.getScheduledTime());
        scheduleQuartzJob(campaignHeader);

        return campaignHeaderRepository.save(campaignHeader);
    }

    public List<CampaignHeader> getScheduledCampaignsToExecute() {
        Specification<CampaignHeader> campaignHeaderSpecification = Specification.where(CampaignHeaderSpecification.statusEqual(SCHEDULED))
                .and(CampaignHeaderSpecification.scheduledTime(System.currentTimeMillis()));

        return campaignHeaderRepository.findAll(campaignHeaderSpecification);
    }

    public Boolean existsByName(String name) {
        return campaignHeaderRepository.existsByName(name);
    }

    private void scheduleQuartzJob(CampaignHeader campaignHeader) throws SchedulerException {
        String jobId = "campaign-job-" + campaignHeader.getId().toString();

        JobDetail jobDetail = JobBuilder.newJob(CampaignExecutionJob.class)
                .withIdentity(jobId, "campaign-jobs")
                .usingJobData("campaignId", campaignHeader.getId().toString())
                .build();

        Date scheduledDate = new Date(campaignHeader.getScheduledTime());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("campaign-trigger-" + campaignHeader.getId(), "campaign-triggers")
                .startAt(scheduledDate)
                .build();

        quartzScheduler.scheduleJob(jobDetail, trigger);

        campaignHeader.setQuartzJobId(jobId);
        campaignHeader.setScheduledAt(System.currentTimeMillis());

        campaignHeaderRepository.save(campaignHeader);
    }

}