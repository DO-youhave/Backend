package com.backend.doyouhave.service;

import com.backend.doyouhave.domain.post.Category;
import com.backend.doyouhave.domain.post.Post;
import com.backend.doyouhave.domain.post.dto.*;
import com.backend.doyouhave.domain.user.User;
import com.backend.doyouhave.domain.user.UserLikes;
import com.backend.doyouhave.domain.user.UserState;
import com.backend.doyouhave.exception.NotFoundException;
import com.backend.doyouhave.repository.post.CategoryRepository;
import com.backend.doyouhave.repository.post.PostRepository;
import com.backend.doyouhave.repository.user.UserLikesRepository;
import com.backend.doyouhave.repository.user.UserRepository;
import com.backend.doyouhave.util.CloudManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Service
public class PostService {
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final UserLikesRepository userLikesRepository;
    private final UserRepository userRepository;
    private final CloudManager cloudManager;
    private final long BANNED_COUNT = 3;

    /* 전단지 작성 */
    public Long savePost(Long userId, Post savedPost, MultipartFile imageFile, MultipartFile imageFileSecond) throws IOException {

        savedPost.setUser(userRepository.findById(userId).orElseThrow());

        // 카테고리 별로 태그 저장. 카테고리가 기존에 존재하는 키워드라면 해당 카테고리에 속하는 태그들을 추가하는 방식
        Category categoryResult = categoryRepository.findByKeyword(savedPost.getCategory());
        if (categoryResult == null) {
            List<String> tagList = Arrays.stream(savedPost.getTags().split(",")).toList();
            categoryResult = Category.builder()
                    .keyword(savedPost.getCategory())
                    .count(0)
                    .tags(tagList)
                    .build();
        } else {
            List<String> tags = categoryResult.getTags();
            List<String> postTags = Arrays.stream(savedPost.getTags().split(",")).toList();
            for (String tag : postTags) {
                tags.add(tag);
            }
        }
        categoryResult.setCount(categoryResult.getCount() + 1);
        categoryRepository.save(categoryResult);

        // 클라우디너리에 이미지 저장 및 글 작성 처리
        postRepository.save(savedPost);
        cloudManager.uploadFile(savedPost.getId(), imageFile, imageFileSecond, savedPost);

        return savedPost.getId();
    }

    /* 전단지 삭제 */
    public void deletePost(Long userId, Long postId) throws IOException {
        Post foundedPost = postRepository.findById(postId).orElseThrow(
                () -> new NotFoundException());
        // 전단지가 삭제되면 작성자가 입력한 게시글의 해당 카테고리 태그들도 삭제됨
        deleteCategoryTags(foundedPost);
        foundedPost.remove(userRepository.findById(userId).orElseThrow());
        cloudManager.deleteFile(foundedPost, foundedPost.getImg(), foundedPost.getImgSecond());
        postRepository.delete(foundedPost);
    }

    /* 전단지 수정 정보 반환 */
    public PostUpdateResponseDto getPostInfoForUpdate(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException());
        PostUpdateResponseDto updateResponse = PostUpdateResponseDto.builder().entity(post).build();
        return updateResponse;
    }

    /* 전단지 수정 처리 */
    public Post updatePost(Long postId, PostUpdateRequestDto updateRequestDto,
                           MultipartFile updateImage, MultipartFile updateImageSecond) throws IOException {
        Post foundedPost = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException());
        // 카테고리 테이블에 존재하는 태그 수정 처리
        deleteCategoryTags(foundedPost);
        updateCategoryTags(updateRequestDto);

        String beforeImage = foundedPost.getImg();
        String beforeImageSecond = foundedPost.getImgSecond();
        cloudManager.uploadFile(postId, updateImage, updateImageSecond, foundedPost);
        cloudManager.deleteFile(foundedPost, beforeImage, beforeImageSecond);

        foundedPost.update(updateRequestDto);
        return postRepository.save(foundedPost);
    }

    /* 삭제한 게시글의 카테고리 태그 삭제 (카테고리는 고정되어 있으므로 태그만 삭제 처리) */
    private void deleteCategoryTags(Post foundedPost) {
        Category beforeCategory = categoryRepository.findByKeyword(foundedPost.getCategory());
        List<String> tags = beforeCategory.getTags();
        List<String> postTags = Arrays.stream(foundedPost.getTags().split(",")).toList();
        for (String tag : postTags) {
            tags.remove(tag);
        }
        beforeCategory.setCount(beforeCategory.getCount() - 1);
    }

    /* 업데이트한 게시글의 카테고리 태그 수정 */
    private void updateCategoryTags(PostUpdateRequestDto updateRequestDto) {
        Category afterCategory = categoryRepository.findByKeyword(updateRequestDto.getCategoryKeyword());
        if (afterCategory == null) {
            List<String> tagList = Arrays.stream(updateRequestDto.getTags().split(",")).toList();
            afterCategory = Category.builder()
                    .keyword(updateRequestDto.getCategoryKeyword())
                    .count(0)
                    .tags(tagList)
                    .build();
        } else {
            List<String> newTags = afterCategory.getTags();
            List<String> updatedTags = Arrays.stream(updateRequestDto.getTags().split(",")).toList();
            for (String tag : updatedTags) {
                newTags.add(tag);
            }
        }
        afterCategory.setCount(afterCategory.getCount() + 1);
        categoryRepository.save(afterCategory);
    }

    /* 메인 페이지에 전달되는 전단지 개수 */
    public Map<String, Integer> findCountPost() {
        int allCount = 0; // 전체 전단지 개수
        int todayCount = 0; // 오늘 붙여진 전단지 개수

        for (Post post : postRepository.findAll()) {
            allCount += 1;
            if (LocalDate.now().isEqual(post.getCreatedDate().toLocalDate())) {
                todayCount += 1;
            }
        }

        Map<String, Integer> postCounts = new HashMap<>();
        postCounts.put("allCount", allCount);
        postCounts.put("todayCount", todayCount);
        return postCounts;
    }

    /* 인기 태그 상위 5개 조회 */
    public List<String> findTopTags(String category) {
        List<String> topTags = null;
        if(category == null) {
            topTags = categoryRepository.findTop5ByTags();
        } else {
            topTags = categoryRepository.findTop5ByCategoryAndTags(category);
        }
        return topTags;
    }

    /* 마이페이지에서 사용자가 작성한 전단지 목록 반환 */
    public Page<PostListResponseDto> findUsersPostByUserId(Long userId, Pageable pageable) {
        Page<PostListResponseDto> postResultList = null;

        postResultList = postRepository.findByUserId(userId, pageable).map(PostListResponseDto::new);

        return postResultList;
    }

    /* 북마크 처리 */
    public void markPost(Long postId, Long userId) {
        User user =  userRepository.findById(userId).orElseThrow();
        Post markedPost = postRepository.findById(postId).orElseThrow();
        LocalDateTime originPostTime = markedPost.getCreatedDate();

        UserLikes userLikes = UserLikes.builder()
                                        .markedUser(user)
                                        .markedPost(markedPost)
                                        .build();
        userLikes.setUser(user);
        userLikes.setPost(markedPost);
        userLikesRepository.save(userLikes);
        markedPost.setCreatedDate(originPostTime);
    }

    /* 북마크 제거 */
    public void markDeletePost(Long postId, Long userId) {
        UserLikes deleteMark = userLikesRepository.findNotOptionalByUserIdAndPostId(userId, postId);
        deleteMark.deleteUserAndPost(userRepository.findById(userId).orElseThrow(), postRepository.findById(postId).orElseThrow());

        userLikesRepository.delete(deleteMark);
    }

    /* 게시글 정보 반환 */
    public PostInfoDto getPostInfo(Long userId, Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        Boolean mark = userId != null && userLikesRepository.findByUserIdAndPostId(userId, postId).isPresent();
        Long markNum = (long) post.getUserLikes().size();

        post.setCommentNum(post.getCommentList().size());
        post.setViewCount(post.getViewCount()+1);
        postRepository.save(post);

        return PostInfoDto.from(post, userId, mark, markNum);
    }

    /* 신고 처리 */
    public void reportedPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(NotFoundException::new);
        post.setReportedCount(post.getReportedCount() + 1);
        // 게시글당 신고 횟수가 3회 이상인 경우 계정 제한 상태로 변경
        if(post.getReportedCount() >= BANNED_COUNT) {
            post.getUser().setUserState(UserState.SUSPENDED);
        }
        postRepository.save(post);
    }

    /* 신고된 전단지 리스트 반환 */
    public Page<ReportedPostDto> findReportedPosts(Pageable pageable) {
        return postRepository.findAllByReportedPost(pageable).map(ReportedPostDto::new);
    }
}