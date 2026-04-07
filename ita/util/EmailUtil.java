package ita.util;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Pattern;

public final class EmailUtil {
    private EmailUtil() {throw new IllegalStateException("Utility class");}

    public static boolean isEmailAddressValid(String emailAddress) {

        //String regexPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*+@(?:[a-zA-Z0-9-]+\\.)++[a-zA-Z]{2,7}$";
        //return Pattern.compile(regexPattern).matcher(emailAddress).matches();
        return EmailValidator.getInstance().isValid(emailAddress);
    }
}
