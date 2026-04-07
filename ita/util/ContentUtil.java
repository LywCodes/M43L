package ita.util;

import org.apache.commons.text.StringSubstitutor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ContentUtil {
    private ContentUtil() { throw new IllegalStateException("Utility class"); }
    public static String decodeBase64(String base64String) {

        byte[] decodedBytes = Base64.getDecoder().decode(base64String);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private static final Pattern EXTRACT_PATTERN = Pattern.compile("\\{\\{\\s*+(\\w++)(?::-[^}]*+)?\\s*+\\}\\}");

    public static Set<String> extractKeys(String templateHtml){
        Set<String> keys = new HashSet<>();
        if (templateHtml == null) return keys;

        Matcher matcher = EXTRACT_PATTERN.matcher(templateHtml);
        while (matcher.find()){
            keys.add(matcher.group(1).trim());
        }
        return keys;
    }

    public static String replaceKeys(String templateHtml, Map<String, String> attributes){
        if(templateHtml == null || templateHtml.isEmpty()) return "";
        if (attributes == null) return templateHtml;

        StringSubstitutor substitutor = new StringSubstitutor(attributes);

        substitutor.setVariablePrefix("{{");
        substitutor.setVariableSuffix("}}");
        substitutor.setValueDelimiter(":-");  //default value

        return substitutor.replace(templateHtml);
    }
}
