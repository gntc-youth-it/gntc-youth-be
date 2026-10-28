package com.gntcyouthbe.thanks.service;

import com.gntcyouthbe.common.exception.EntityNotFoundException;
import com.gntcyouthbe.common.exception.ForbiddenException;
import com.gntcyouthbe.common.security.domain.UserPrincipal;
import com.gntcyouthbe.thanks.domain.ThanksComment;
import com.gntcyouthbe.thanks.domain.ThanksEntry;
import com.gntcyouthbe.thanks.model.request.ThanksCommentCreateRequest;
import com.gntcyouthbe.thanks.model.response.ThanksCommentListResponse;
import com.gntcyouthbe.thanks.model.response.ThanksCommentResponse;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.CommentSummary;
import com.gntcyouthbe.thanks.model.response.ThanksEntryResponse.LatestCommentInfo;
import com.gntcyouthbe.thanks.repository.ThanksCommentRepository;
import com.gntcyouthbe.thanks.repository.ThanksEntryRepository;
import com.gntcyouthbe.user.domain.Role;
import com.gntcyouthbe.user.domain.User;
import com.gntcyouthbe.user.repository.UserProfileRepository;
import com.gntcyouthbe.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_COMMENT_ACCESS_DENIED;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_COMMENT_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.THANKS_ENTRY_NOT_FOUND;
import static com.gntcyouthbe.common.exception.model.ExceptionCode.USER_NOT_FOUND;

// 댓글을 달거나 지우면 그 감사의 댓글 전체를 돌려준다. 그사이 다른 사람이 단 댓글도 보이고,
// FE가 목록 카드의 댓글 수와 최근 댓글도 맞출 수 있다.
@Service
@RequiredArgsConstructor
public class ThanksCommentService {

    private final ThanksCommentRepository thanksCommentRepository;
    private final ThanksEntryRepository thanksEntryRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional(readOnly = true)
    public ThanksCommentListResponse getComments(UserPrincipal userPrincipal, Long entryId) {
        if (!thanksEntryRepository.existsById(entryId)) {
            throw new EntityNotFoundException(THANKS_ENTRY_NOT_FOUND);
        }
        return commentsOf(entryId, userPrincipal.getUserId());
    }

    // 감사 행은 잠그지 않는다. 감사를 지우는 바로 그 순간에 단 댓글은 짝 없이 남을 수 있지만, 어느 화면에도 나오지 않는다.
    @Transactional
    public ThanksCommentListResponse addComment(UserPrincipal userPrincipal, Long entryId,
            ThanksCommentCreateRequest request) {
        ThanksEntry entry = thanksEntryRepository.findById(entryId)
                .orElseThrow(() -> new EntityNotFoundException(THANKS_ENTRY_NOT_FOUND));
        User user = userRepository.findById(userPrincipal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        thanksCommentRepository.save(new ThanksComment(entry, user, request.content()));
        return commentsOf(entryId, userPrincipal.getUserId());
    }

    // 쓴 사람 본인이나 MASTER만 지운다. 감사를 올린 사람이라도 남의 댓글은 지울 수 없다.
    // 권한 오류는 401로 바뀌지 않도록 @PreAuthorize가 아니라 여기서 403으로 던진다.
    @Transactional
    public ThanksCommentListResponse deleteComment(UserPrincipal userPrincipal, Long commentId) {
        ThanksComment comment = thanksCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(THANKS_COMMENT_NOT_FOUND));
        if (!comment.isWrittenBy(userPrincipal.getUserId()) && userPrincipal.getRole() != Role.MASTER) {
            throw new ForbiddenException(THANKS_COMMENT_ACCESS_DENIED);
        }

        Long entryId = comment.getEntry().getId();
        thanksCommentRepository.delete(comment);
        return commentsOf(entryId, userPrincipal.getUserId());
    }

    // 목록 카드용 감사별 댓글 수와 가장 최근 댓글. 감사마다 따로 조회하지 않는다.
    // 두 값을 한 쿼리로 가져온다. 따로 조회하면 그사이 달린 첫 댓글이 개수 없이 최근 댓글로만 잡혀서 목록이 500이 될 수 있다.
    // 댓글이 없는 감사는 결과에 넣지 않는다.
    @Transactional(readOnly = true)
    public Map<Long, CommentSummary> summarize(List<Long> entryIds) {
        if (entryIds.isEmpty()) {
            return Map.of();
        }
        List<LatestComment> latestComments = thanksCommentRepository.findLatestWithCountByEntryIds(entryIds).stream()
                .map(LatestComment::from)
                .toList();
        Map<Long, String> profileImagePaths = userProfileRepository.findProfileImagePaths(
                latestComments.stream().map(latest -> latest.comment().getUser().getId()).distinct().toList());

        return latestComments.stream()
                .collect(Collectors.toMap(
                        latest -> latest.comment().getEntry().getId(),
                        latest -> new CommentSummary(
                                latest.count(),
                                LatestCommentInfo.of(
                                        latest.comment(), profileImagePaths.get(latest.comment().getUser().getId())))));
    }

    // findLatestWithCountByEntryIds의 한 줄: [가장 최근 댓글, 그 감사의 댓글 수]
    private record LatestComment(ThanksComment comment, long count) {
        static LatestComment from(Object[] row) {
            return new LatestComment((ThanksComment) row[0], (Long) row[1]);
        }
    }

    private ThanksCommentListResponse commentsOf(Long entryId, Long viewerId) {
        List<ThanksComment> comments = thanksCommentRepository.findByEntryIdWithUser(entryId);
        Map<Long, String> profileImagePaths = userProfileRepository.findProfileImagePaths(
                comments.stream().map(comment -> comment.getUser().getId()).distinct().toList());

        List<ThanksCommentResponse> responses = comments.stream()
                .map(comment -> ThanksCommentResponse.of(
                        comment, profileImagePaths.get(comment.getUser().getId()), viewerId))
                .toList();
        return new ThanksCommentListResponse(entryId, responses);
    }
}
