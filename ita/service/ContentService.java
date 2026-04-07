package ita.service;

import io.micrometer.tracing.Tracer;
import ita.dto.BaseSearchCriteriaDto;
import ita.dto.ContentRequestDto;
import ita.entity.Content;
import ita.exception.NotFoundException;
import ita.projection.BaseProjection;
import ita.repository.ContentRepository;
import ita.util.AuthUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static ita.enumeration.EntityType.CONTENT_TYPE;
import static ita.enumeration.OperationType.ADD_OPERATION;

@Service
@Slf4j
public class ContentService {

    private String serviceName;
    private Tracer tracer;

    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository,
                          Tracer tracer,
                          @Value("${spring.application.name}") String serviceName ) {
        this.contentRepository = contentRepository;
        this.tracer = tracer;
        this.serviceName = serviceName;
    }

    public Page<BaseProjection> findAllContent(BaseSearchCriteriaDto searchCriteria) {
        int pageSize = (searchCriteria.getSize() == 999) ? Integer.MAX_VALUE : searchCriteria.getSize();

        Sort sort = Sort.by(searchCriteria.getParam());
        Pageable pageable = PageRequest.of(searchCriteria.getPage(), pageSize,
                searchCriteria.getType().equalsIgnoreCase("desc") ? sort.descending() : sort.ascending());

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

        log.info("Request Body: {}", content);

        return contentRepository.save(content);
    }

    public Boolean existsByName(String name) {
        return contentRepository.existsByName(name);
    }

    @Transactional
    public void deleteContent(String id) {
        UUID contentId = UUID.fromString(id);

        contentRepository.deleteById(contentId);
    }

}
