package com.gntcyouthbe.common.security.domain;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.user.domain.AuthProvider;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

@AllArgsConstructor
@Getter
public class UserPrincipal implements UserDetails, OAuth2User, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;

    private final String email;

    private final String name;

    private final Role role;

    private final ChurchId church;

    private final AuthProvider provider;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long userId, String email, String name, Role role, ChurchId church,
            AuthProvider provider) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.church = church;
        this.provider = provider;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name()));
    }

    public UserPrincipal(User user) {
        this(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getChurchId(),
                user.getProvider()
        );
    }

    public UserPrincipal(Map<String, Object> claims) {
        this(
                Long.valueOf((String) claims.get("sub")),
                (String) claims.get("email"),
                (String) claims.get("name"),
                Role.valueOf((String) claims.get("role")),
                claims.containsKey("church") ?
                        ChurchId.valueOf((String) claims.get("church")) : null,
                claims.containsKey("provider") ?
                        AuthProvider.valueOf((String) claims.get("provider")) : null
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    @Override
    public String getName() {
        return name;
    }
}
