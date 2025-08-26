package com.gntcyouthbe.common.jpa.component;

import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext()
                .getAuthentication();
        if (isValidAuthentication(auth)) {
            return Optional.empty();
        }
//        return Optional.ofNullable(auth);
        return Optional.of(1L);
    }

    private boolean isValidAuthentication(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth.getPrincipal()
                .equals("anonymousUser");
    }
}
