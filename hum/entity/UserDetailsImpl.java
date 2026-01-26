package ita.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder
@Slf4j
public class UserDetailsImpl implements UserDetails {

    private UUID id;
    private String name;
    private String username;
    private String password;
    private Set<SimpleGrantedAuthority> permissions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public static UserDetailsImpl build(LocalUser user) {
        Set<SimpleGrantedAuthority> distinctPermissions = new HashSet<>();

        Set<LocalRole> roles = user.getRoles();

        for(LocalRole role : roles) {
            for (Permission permission : role.getPermissions()) {
                distinctPermissions.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }

        return new UserDetailsImpl(user.getId(), user.getUsername(), distinctPermissions);
    }

    public UserDetailsImpl(UUID id, String username, Set<SimpleGrantedAuthority> permissions) {
        this.id = id;
        this.username = username;
        this.password = null;
        this.permissions = permissions;
    }

}
