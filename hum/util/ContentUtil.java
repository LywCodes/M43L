package ita.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ContentUtil {

    public static String decodeBase64(String base64String) {

        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

}
