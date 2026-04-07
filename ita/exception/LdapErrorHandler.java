package ita.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import ita.dto.LdapResponseDto;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

@Component
public class LdapErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() != HttpStatusCode.valueOf(200);
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        LdapResponseDto ldapResponseDto = objectMapper.readValue(response.getBody(), LdapResponseDto.class);

        throw new BadCredentialsException(ldapResponseDto.getErrorSchema().getErrorMessage().getEnglish());
    }
}
