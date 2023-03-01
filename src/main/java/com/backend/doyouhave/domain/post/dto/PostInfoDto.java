package com.backend.doyouhave.domain.post.dto;

import com.backend.doyouhave.domain.post.Post;
import com.backend.doyouhave.domain.user.dto.LoginResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
public class PostInfoDto {

    @ApiModelProperty(value = "1")
    private Long postId;
    @ApiModelProperty(value = "2022~~")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createdDate;
    @ApiModelProperty(value = "1")
    private Long writerId;
    @ApiModelProperty(value = "true")
    private Boolean isWriter;
    @ApiModelProperty(value = "고민이 있어요")
    private String title;
    @ApiModelProperty(value = "테스트")
    private String content;
    @ApiModelProperty(value = "KAKAO,EMAIL")
    private String contactWay;
    @ApiModelProperty(value = "http://open.kakao.com/o/sDMnCBS")
    private String kakaoUrl;
    @ApiModelProperty(value = "abcd@naver.com")
    private String email;
    @ApiModelProperty(value = "MEDICAL")
    private String categoryKeyword;
    @ApiModelProperty(value = "['test1', 'test2', 'test3']")
    private List<String> tags;
    private String img;
    private String imgSecond;
    @ApiModelProperty(value = "0")
    private Long viewCount;
    @ApiModelProperty(value = "0")
    private Long commentNum;
    @ApiModelProperty(value = "false")
    private Boolean mark;
    @ApiModelProperty(value = "0")
    private Long markNum;

    @Builder
    public PostInfoDto(Post entity, Long userId, Boolean mark, Long markNum) {
        this.postId = entity.getId();
        this.createdDate = entity.getCreatedDate();
        this.writerId = entity.getUser().getId();
        this.isWriter = userId != null && Objects.equals(entity.getUser().getId(), userId);
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.contactWay = entity.getContactWay();
        this.kakaoUrl = entity.getKakaoUrl();
        this.email = entity.getEmail();
        this.categoryKeyword = entity.getCategory();
        this.tags = Arrays.stream(entity.getTags().split(",")).toList();
        this.img = entity.getImg();
        this.imgSecond = entity.getImgSecond();
        this.viewCount = entity.getViewCount();
        this.commentNum = entity.getCommentNum();
        this.mark = mark;
        this.markNum = markNum;
    }

    public static PostInfoDto from(Post entity, Long userId, Boolean mark, Long markNum) {
        return PostInfoDto.builder()
                .entity(entity)
                .userId(userId)
                .mark(mark)
                .markNum(markNum)
                .build();
    }
}