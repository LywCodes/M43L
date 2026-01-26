package ita.util;

import ita.dto.ErrorSchema;
import ita.dto.ResponseDto;
import ita.enumeration.EntityType;

import java.util.HashMap;
import java.util.Map;


public class ResponseDtoUtil {

    public static ResponseDto<Object> generateResponse(String code, String message, Object payload) {
        ErrorSchema errorSchema = new ErrorSchema(code, message);

        return new ResponseDto<>(errorSchema, payload);
    }

    public static Map<String, String> generateDeleteMessage(String id, EntityType entityType) {
        Map<String, String> payload = new HashMap<>();

        payload.put("message", String.format("%s with id %s is deleted", entityType.getValue(), id));

        return payload;
    }

    public static Map<String, String> generatePayload(String exceptionMessage) {
        Map<String, String> payload = new HashMap<>();

        payload.put("message", exceptionMessage);

        return payload;
    }

}
