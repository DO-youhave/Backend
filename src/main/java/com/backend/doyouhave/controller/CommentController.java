package com.backend.doyouhave.controller;

import com.backend.doyouhave.domain.comment.dto.CommentRequestDto;
import com.backend.doyouhave.domain.comment.dto.CommentInfoDto;
import com.backend.doyouhave.domain.comment.dto.CommentResponseDto;
import com.backend.doyouhave.domain.comment.dto.MyInfoCommentResponseDto;
import com.backend.doyouhave.domain.post.dto.PostResponseDto;
import com.backend.doyouhave.service.CommentService;
import com.backend.doyouhave.service.result.ResponseService;
import com.backend.doyouhave.service.result.Result;
import com.backend.doyouhave.service.result.SingleResult;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ResponseService responseService;

    /* 댓글 생성 API */
    @PostMapping("/posts/{postId}")
    @ApiOperation(value = "댓글 생성")
    public ResponseEntity<SingleResult<PostResponseDto>> saveComment(@PathVariable("postId") Long postId,
                                                                     @AuthenticationPrincipal Long userId,
                                                                     @RequestBody CommentRequestDto commentRequestDto) {
        commentService.save(commentRequestDto, userId, postId);

        return ResponseEntity.ok(responseService.getSingleResult(new PostResponseDto(postId)));
    }

    /* 댓글 수정 API */
    @PostMapping("/posts/{postId}/{commentId}")
    @ApiOperation(value = "댓글 수정", response = SingleResult.class)
    public ResponseEntity<SingleResult<PostResponseDto>> updateComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
                                                      @RequestBody CommentRequestDto commentRequestDto) {
        commentService.update(commentId, commentRequestDto);

        return ResponseEntity.ok(responseService.getSingleResult(new PostResponseDto(postId)));
    }

    /* 댓글 삭제 API */
    @DeleteMapping("/posts/{postId}/{commentId}")
    @ApiOperation(value = "댓글 삭제", response = SingleResult.class)
    public ResponseEntity<SingleResult<PostResponseDto>> deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {

        commentService.delete(commentId);

        return ResponseEntity.ok(responseService.getSingleResult(new PostResponseDto(postId)));
    }

    /* 내가 쓴 댓글 API */
    @GetMapping("/mypage/comments")
    @ApiOperation(value = "내가 쓴 댓글 목록", notes = "내가 쓴 댓글 목록 출력", response = MyInfoCommentResponseDto.class)
    public ResponseEntity<?> myComments(@AuthenticationPrincipal Long userId, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(commentService.findByUser(userId, pageable));
    }

    /* 댓글 목록 반환 API */
    @GetMapping("/posts/{postId}/comments")
    @ApiOperation(value = "댓글 목록 반환")
    public ResponseEntity<SingleResult<CommentResponseDto>> getCommentsByPost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : #this") Long userId,
            @PageableDefault(size=10) Pageable pageable
    ) {
        boolean isWriter = commentService.checkUserIsWriter(postId, userId);
        Page<CommentInfoDto> commentsByPost = commentService.getCommentsByPost(postId, userId, pageable);
        return ResponseEntity.ok(responseService.getSingleResult(CommentResponseDto.from(isWriter, commentsByPost)));
    }

    /* 댓글 신고 처리 API */
    @PostMapping("/comments/{commentId}/report")
    @ApiOperation(value = "댓글 신고 처리")
    public ResponseEntity<Result> reportComment(@PathVariable Long commentId) {
        commentService.reportedComment(commentId);
        return ResponseEntity.ok(responseService.getSuccessResult());
    }
}
