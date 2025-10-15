package com.gntcyouthbe.common.orm.component;

import com.gntcyouthbe.common.security.domain.UserPrincipal;
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
        if (isInvalidAuthentication(auth)) {
            return Optional.empty();
        }
        Object p = auth.getPrincipal();
        if (p instanceof UserPrincipal up) return Optional.of(up.getUserId());
        return Optional.empty();
    }

    private boolean isInvalidAuthentication(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth.getPrincipal()
                .equals("anonymousUser");
    }
}
