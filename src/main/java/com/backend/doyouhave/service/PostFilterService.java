package com.backend.doyouhave.service;

import com.backend.doyouhave.domain.post.dto.PostListResponseDto;
import com.backend.doyouhave.repository.post.PostRepository;
import com.backend.doyouhave.service.sorttype.CommentCount;
import com.backend.doyouhave.service.sorttype.Date;
import com.backend.doyouhave.service.sorttype.SortWay;
import com.backend.doyouhave.service.sorttype.View;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Transactional
@RequiredArgsConstructor
@Service
public class PostFilterService {
    private final PostRepository postRepository;

    /* 전단지 골목 페이지에서 카테고리 또는 태그에 따라 전단지 기본 정보 리스트 반환 */
    public Page<PostListResponseDto> findPostByCategoryOrTags(String category, String tag, String sort, Pageable pageable) {
        Page<PostListResponseDto> postResultList = null;
        Map<String, SortWay> sortTypeMap = new HashMap<>();

        // 정렬 기본값은 최근 작성일순
        sortTypeMap.put(null, new Date(postRepository));
        sortTypeMap.put("DATE", new Date(postRepository));
        sortTypeMap.put("VIEW", new View(postRepository));
        sortTypeMap.put("COMMENT", new CommentCount(postRepository));
        postResultList = sortTypeMap.get(sort).findPostBySortType(category, tag, pageable);

        return postResultList;
    }

    /* 제목 기준 전단지 검색용 */
    public Page<PostListResponseDto> findPostByCategoryAndSearch(String category, String search, String sort, Pageable pageable) {
        Page<PostListResponseDto> postResultList = null;
        try {
            PageRequest request
                    = sort == null || sort.equals("DATE") ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "created_date"))
                    : sort.equals("VIEW") ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "view_count"))
                    : sort.equals("COMMENT") ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "comment_num")) : null;

            postResultList = postRepository.findByCategoryAndSearch(category, search, search, search, request).map(PostListResponseDto::new);
        } catch (NoSuchFieldError e) {
            e.printStackTrace();
        }
        return postResultList;
    }

}
