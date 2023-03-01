package com.backend.doyouhave.domain.post.dto;

import com.backend.doyouhave.domain.post.Post;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostRequestDto {
    @ApiModelProperty(value = "고민이 있어요")
    @NotNull
    private String title;
    @ApiModelProperty(value = "테스트 내용")
    @NotNull
    private String content;
    @ApiModelProperty(value = "KAKAO,EMAIL")
    @NotNull
    private String contactWay;
    @ApiModelProperty(value = "http://open.kakao.com/o/sDMnCBS")
    private String kakaoUrl;
    @ApiModelProperty(value = "abcd@naver.com")
    private String email;
    @ApiModelProperty(value = "공부")
    @NotNull
    private String categoryKeyword;
    @ApiModelProperty(value = "스프링,자바,프로젝트")
    private String tags;


    @Builder
    public PostRequestDto(String title, String content, String contactWay, String kakaoUrl, String email, String categoryKeyword, String tags) {
        this.title = title;
        this.content = content;
        this.contactWay = contactWay;
        this.kakaoUrl = kakaoUrl;
        this.email = email;
        this.categoryKeyword = categoryKeyword;
        this.tags = tags;
    }

    public Post toEntity() {
        Post post = new Post();
        post.create(title, content, contactWay, kakaoUrl, email, categoryKeyword, tags);
        return post;
    }
}
