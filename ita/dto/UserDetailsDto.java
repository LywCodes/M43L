package ita.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
public class UserDetailsDto {

    private UUID id;
    private String username;
    private Set<SimpleGrantedAuthority> permissions;

}
