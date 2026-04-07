package ita.controller;

import ita.dto.AttributeValidationResponseDto;
import ita.dto.ResponseDto;
import ita.property.ResponseProperty;
import ita.service.ContentValidationService;
import ita.util.ResponseDtoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("api/content/validation")
@RequiredArgsConstructor
@Slf4j
public class ContentValidationController {
    private final ContentValidationService contentValidationService;
    private final ResponseProperty responseProperty;

    @GetMapping("/content-compatibility")
    public ResponseEntity<ResponseDto<Object>>validateContentCompatibility(
            @RequestParam UUID contentId,
            @RequestParam UUID groupId
    ){
        log.info("validating compatibility for content ID: {}  against group ID: {}", contentId, groupId);

        AttributeValidationResponseDto result = contentValidationService.validateContentCompatibility(contentId, groupId);

        ResponseDto<Object> responseDto = ResponseDtoUtil.generateResponse(
                responseProperty.getSuccess().getCode().getContent(),
                responseProperty.getSuccess().getMessage().getContent(),
                result
        );
        return ResponseEntity.status(200).body(responseDto);
    }
}
