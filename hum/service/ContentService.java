package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContentRequestDto;
import ita.entity.Contact;
import ita.entity.Content;
import ita.exception.NotFoundException;
import ita.projection.BaseProjection;
import ita.repository.ContentRepository;
import ita.specification.ContactSpecification;
import ita.specification.ContentSpecification;
import ita.util.AuthUtil;
import ita.util.HtmlSanitizerUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.ATTACHMENT_TYPE;
import static ita.enumeration.EntityType.CONTENT_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;

@Service
@Slf4j
public class ContentService {

    @Value("${spring.application.name}")
    private String serviceName;

    @Autowired
    private Tracer tracer;

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public Page<BaseProjection> findAllContent(BaseSearchCriteriaDto searchCriteria) {
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

        return contentRepository.findProjection(pageable, searchCriteria.getName());
    }

    public Content findById(UUID id) {
        Optional<Content> content = contentRepository.findById(id);

        if (content.isEmpty()) {
            throw new NotFoundException(CONTENT_TYPE, "id", id.toString());
        }

        return content.get();
    }

    public Content addContent(ContentRequestDto contentRequestDto) {
        Content content = new Content();

        content.setName(contentRequestDto.getName());
        content.setHtml(contentRequestDto.getHtml());
        content.setNumberOfParam(contentRequestDto.getNumberOfParam());
        content.setCreatedAt(System.currentTimeMillis());

        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

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

        log.info("Request Body: {}", content);

        return contentRepository.save(content);
    }

    @Transactional
    public void deleteContent(String id) {
        UUID contentId = UUID.fromString(id);

        contentRepository.deleteById(contentId);
    }

}
