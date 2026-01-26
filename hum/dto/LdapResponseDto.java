package ita.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class LdapResponseDto {
    private ErrorSchema errorSchema;
    private OutputSchema outputSchema;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ErrorSchema {
        private String errorCode;
        private ErrorMessage errorMessage;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class ErrorMessage {
            private String indonesian;
            private String english;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class OutputSchema {
        private String status;
    }

}
