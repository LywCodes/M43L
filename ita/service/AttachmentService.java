package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.*;
import ita.entity.Attachment;
import ita.exception.NotFoundException;
import ita.projection.BaseProjection;
import ita.repository.AttachmentRepository;
import ita.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;


@Service
@Slf4j
public class AttachmentService {

    private final String serviceName;
    private final Tracer tracer;
    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             @Value("${spring.application.name}") String serviceName,
                             Tracer tracer) {
        this.attachmentRepository = attachmentRepository;
        this.serviceName = serviceName;
        this.tracer = tracer;
    }

    public Page<BaseProjection> findByFilter(BaseSearchCriteriaDto searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();
        String sortParam = searchCriteria.getParam();
        Sort sort = searchCriteria.getType().equalsIgnoreCase("desc") ? Sort.by(sortParam).descending() : Sort.by(sortParam).ascending();
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize, sort);

        return attachmentRepository.findProjection(pageable, searchCriteria.getName());
    }

    public AttachmentResponseDto findDtoById(UUID id) {
        Attachment attachment = findById(id);

        AttachmentResponseDto attachmentResponseDto = new AttachmentResponseDto();

        attachmentResponseDto.setId(attachment.getId());
        attachmentResponseDto.setName(attachment.getName());

        return attachmentResponseDto;
    }

    public void uploadAttachment(AttachmentRequestDto attachmentRequestDto) throws IOException, NoSuchMethodException {
        Method method = this.getClass().getDeclaredMethod("uploadAttachment", AttachmentRequestDto.class);

        Attachment attachment = new Attachment();

        attachment.setName(attachmentRequestDto.getName());
        attachment.setFile(attachmentRequestDto.getFile().getBytes());
        attachment.setCreatedAt(System.currentTimeMillis());

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

        attachmentRepository.save(attachment);
    }

    public void updateAttachment(AttachmentUpdateDto attachmentUpdateDto) {
        Attachment attachment = findById(attachmentUpdateDto.getId());

        attachment.setName(attachmentUpdateDto.getName());
        attachment.setUpdatedAt(System.currentTimeMillis());

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

    public Boolean existsByName(String name) {
        return attachmentRepository.existsByName(name);
    }

    public boolean isUniqueForUpdate(String name, UUID id) {
        return !attachmentRepository.existsByNameAndNotId(name, id);
    }
}