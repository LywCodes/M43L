package ita.service;

import ita.dto.AttributeValidationResponseDto;
import ita.entity.ContactAttribute;
import ita.entity.Content;
import ita.enumeration.EntityType;
import ita.exception.NotFoundException;
import ita.repository.ContentRepository;
import ita.util.ContentUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentValidationService {

    private final ContentRepository contentRepository;
    private final ContactAttributeService contactAttributeService;
    private static final Set<String> GLOBAL_ATTRIBUTES = Set.of("email", "name", "created_at");

    @Transactional
    public AttributeValidationResponseDto validateContentCompatibility(UUID contentId, UUID groupId){
        Content content = contentRepository
                .findById(contentId)
                .orElseThrow(()-> new NotFoundException(EntityType.CONTENT_TYPE, "id", contentId.toString()));

        String rawHtml = content.getHtml();
        String htmlToScan;

        try {
            htmlToScan = ContentUtil.decodeBase64(rawHtml);

        } catch (IllegalArgumentException e) {
            log.warn("Content ID {} is not Base64 encoded. Using raw string.", contentId);
            htmlToScan = rawHtml;
        }

        Set<String> requiredAttributes = ContentUtil.extractKeys(htmlToScan);

        log.info("Extracted template attributes {}", requiredAttributes);

        if (requiredAttributes.isEmpty()){
            return AttributeValidationResponseDto.builder()
                    .isValid(true)
                    .message("no keys found. Compatible with any groups")
                    .missingAttributes(Collections.emptyList())
                    .build();
        }
        Optional<ContactAttribute>contactSample = contactAttributeService.getSampleByGroupId(groupId);

        if(contactSample.isEmpty()){
            return AttributeValidationResponseDto.builder()
                    .isValid(false)
                    .message("Contact Group is empty. Cannot validate compatibility.")
                    .missingAttributes(new ArrayList<>(requiredAttributes))
                    .build();
        }

        ContactAttribute sampleMember = contactSample.get();
        Map<String, String> availableAttributes = sampleMember.getAttributes();

        log.info("Sample contact attributes (JSONB): {}", availableAttributes);

        List<String> missingAttributes = new ArrayList<>();

        for (String attribute : requiredAttributes) {
            String cleanAttributes = attribute.toLowerCase().trim();

            if (GLOBAL_ATTRIBUTES.contains(cleanAttributes)) {continue;}

            if (availableAttributes == null || !availableAttributes.containsKey(cleanAttributes)) {
                missingAttributes.add(attribute);
                log.warn("Missing Attribute key: '{}'", attribute);

            }else {log.info("match key '{}' found in attributes", attribute);}
        }
        boolean isValid = missingAttributes.isEmpty();


        String message = isValid
                ? "All parameters match with contact group attributes."
                : "Missing attributes found in contact group. Please check your CSV columns.";

        return AttributeValidationResponseDto.builder()
                .isValid(isValid)
                .message(message)
                .missingAttributes(missingAttributes)
                .build();
    }
}
