package ita.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;

@Primary
@Data
@ToString
@AllArgsConstructor
@ConfigurationProperties(prefix = "response")
@ComponentScan(basePackages = {"ita.property"})
public class ResponseProperty {

    private Success success;
    private InvalidInput invalidInput;
    private NotFound notFound;
    private AccessDenied accessDenied;
    private ServerError serverError;

    @Data
    @AllArgsConstructor
    public static class Success {
        private Code code;
        private Message message;

        @Data
        @AllArgsConstructor
        public static class Code {
            private String role;
            private String user;
            private String contact;
            private String contactGroup;
            private String sender;
            private String content;
            private String campaign;
            private String auth;
            private String permission;
            private String unsubscribe;
            private String statistic;
            private String attachment;
        }

        @Data
        @AllArgsConstructor
        public  static class Message {
            private String role;
            private String user;
            private String contact;
            private String contactGroup;
            private String sender;
            private String content;
            private String campaign;
            private String auth;
            private String permission;
            private String unsubscribe;
            private String statistic;
            private String attachment;
        }
    }

    @Data
    @AllArgsConstructor
    public static class InvalidInput {
        private String code;
        private String message;

    }

    @Data
    @AllArgsConstructor
    public static class NotFound {
        private String code;
        private String message;

    }

    @Data
    @AllArgsConstructor
    public static class ServerError {
        private String code;
        private String message;
    }

    @Data
    @AllArgsConstructor
    public static class AccessDenied {

        private Code code;
        private Message message;

        @Data
        @AllArgsConstructor
        public static class Code {
            private String general;
            private String expired;
            private String invalid;
        }

        @Data
        @AllArgsConstructor
        public static class Message {
            private String general;
            private String expired;
            private String invalid;
        }
    }

}
