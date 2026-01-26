package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.AttachmentRequestDto;
import ita.dto.AttachmentResponseDto;
import ita.dto.AttachmentUpdateDto;
import ita.dto.BaseSearchCriteriaDto;
import ita.entity.Attachment;
import ita.exception.NotFoundException;
import ita.projection.BaseProjection;
import ita.repository.AttachmentRepository;
import ita.util.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Tracer tracer;

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public Page<BaseProjection> findByFilter(BaseSearchCriteriaDto searchCriteria) {
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

        return attachmentRepository.findProjection(pageable, searchCriteria.getName());
    }

    public AttachmentResponseDto findDtoById(UUID id) {
        Attachment attachment = findById(id);

        AttachmentResponseDto attachmentResponseDto = new AttachmentResponseDto();

        attachmentResponseDto.setId(attachment.getId());
        attachmentResponseDto.setName(attachment.getName());

        return attachmentResponseDto;
    }

    public void uploadAttachment(AttachmentRequestDto attachmentRequestDto) throws IOException, NoSuchMethodException, MethodArgumentNotValidException {
        Method method = this.getClass().getDeclaredMethod("uploadAttachment", AttachmentRequestDto.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        String attachmentName = attachmentRequestDto.getFile().getOriginalFilename();

        if (attachmentName == null || attachmentName.isBlank()) {
            BeanPropertyBindingResult result = new BeanPropertyBindingResult(attachmentRequestDto, "name");

            result.addError(new FieldError("name",
                    "name",
                    "Must be filled"));

            throw new MethodArgumentNotValidException(parameter, result);
        }

        Attachment attachment = new Attachment();

        attachment.setName(attachmentRequestDto.getFile().getOriginalFilename());
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

        log.info("Request Body: {}", attachment);

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

}
