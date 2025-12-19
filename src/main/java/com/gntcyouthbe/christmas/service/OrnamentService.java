package com.gntcyouthbe.christmas.service;

import com.gntcyouthbe.christmas.domain.Ornament;
import com.gntcyouthbe.christmas.model.request.OrnamentCreateRequest;
import com.gntcyouthbe.christmas.model.response.OrnamentResponse;
import com.gntcyouthbe.christmas.repository.OrnamentRepository;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrnamentService {

    private final OrnamentRepository ornamentRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrnamentResponse createOrnament(final UserPrincipal principal, final OrnamentCreateRequest request) {
        final User user = userRepository.getReferenceById(principal.getUserId());
        final Ornament ornament = new Ornament(
                user,
                request.getWriterName(),
                request.getType(),
                request.getMessage(),
                request.getX(),
                request.getY()
        );
        ornamentRepository.save(ornament);
        return new OrnamentResponse(ornament);
    }

    @Transactional(readOnly = true)
    public List<OrnamentResponse> getAllOrnaments() {
        return ornamentRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrnamentResponse::new)
                .toList();
    }
}

