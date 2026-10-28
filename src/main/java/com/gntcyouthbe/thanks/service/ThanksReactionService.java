package com.gntcyouthbe.thanks.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksEmoji;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.domain.ThanksReaction;
import com.gntcyouthbe.thanks.model.response.ThanksEntryReactionsResponse;
import com.gntcyouthbe.thanks.model.response.ThanksReactionResponse;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.thanks.repository.ThanksReactionCount;
import com.gntcyouthbe.thanks.repository.ThanksReactionRepository;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

// 반응을 바꾸는 요청은 모두 감사 행을 잠근 뒤 확인하고 저장한다. 그래서 같은 반응을 동시에 두 번 눌러도
// unique 위반 없이 한 행만 남는다. unique 위반을 잡아 200으로 돌려주지 않는 이유는, PostgreSQL에서는
// 실패한 INSERT 뒤로 같은 트랜잭션이 막혀서 이어지는 반응 요약 조회가 500이 되기 때문이다.
@Service
@RequiredArgsConstructor
public class ThanksReactionService {

    private final ThanksReactionRepository thanksReactionRepository;
    private final ThanksEntryRepository thanksEntryRepository;
    private final UserRepository userRepository;

    // 이미 켜져 있으면 그대로 둔다
    @Transactional
    public ThanksEntryReactionsResponse addReaction(UserPrincipal userPrincipal, Long entryId, ThanksEmoji emoji) {
        ThanksEntry entry = findEntryForUpdate(entryId);
        Long userId = userPrincipal.getUserId();

        if (!thanksReactionRepository.existsByEntryIdAndUserIdAndEmoji(entryId, userId, emoji)) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
            thanksReactionRepository.save(new ThanksReaction(entry, user, emoji));
        }
        return summaryOf(entryId, userId);
    }

    // 켜져 있지 않으면 그대로 둔다
    @Transactional
    public ThanksEntryReactionsResponse removeReaction(UserPrincipal userPrincipal, Long entryId, ThanksEmoji emoji) {
        findEntryForUpdate(entryId);
        Long userId = userPrincipal.getUserId();

        thanksReactionRepository.deleteByEntryIdAndUserIdAndEmoji(entryId, userId, emoji);
        return summaryOf(entryId, userId);
    }

    // 감사마다 따로 조회하지 않고, 반응별 개수와 내가 누른 반응을 한 번씩만 조회한다.
    // 반응은 FE 크게 보기의 순서(ThanksEmoji 선언 순서)로 담는다.
    @Transactional(readOnly = true)
    public Map<Long, List<ThanksReactionResponse>> summarize(List<Long> entryIds, Long viewerId) {
        if (entryIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Set<ThanksEmoji>> myEmojis = thanksReactionRepository.findByEntryIdInAndUserId(entryIds, viewerId).stream()
                .collect(Collectors.groupingBy(
                        reaction -> reaction.getEntry().getId(),
                        Collectors.mapping(ThanksReaction::getEmoji, Collectors.toSet())));

        return thanksReactionRepository.countByEntryIds(entryIds).stream()
                .sorted(Comparator.comparing(ThanksReactionCount::emoji))
                .collect(Collectors.groupingBy(
                        ThanksReactionCount::entryId,
                        Collectors.mapping(
                                count -> new ThanksReactionResponse(
                                        count.emoji(),
                                        count.count(),
                                        myEmojis.getOrDefault(count.entryId(), Set.of()).contains(count.emoji())),
                                Collectors.toList())));
    }

    private ThanksEntryReactionsResponse summaryOf(Long entryId, Long viewerId) {
        List<ThanksReactionResponse> reactions = summarize(List.of(entryId), viewerId).getOrDefault(entryId, List.of());
        return new ThanksEntryReactionsResponse(entryId, reactions);
    }

    private ThanksEntry findEntryForUpdate(Long entryId) {
        return thanksEntryRepository.findByIdForUpdate(entryId)
                .orElseThrow(() -> new EntityNotFoundException(THANKS_ENTRY_NOT_FOUND));
    }
}
