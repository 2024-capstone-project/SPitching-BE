package djj.spitching_be.Dto;

import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TagListResponseDto {
    private String content;
    private LocalDateTime createdAt;

    // Tag 객체만 받는 생성자
    public TagListResponseDto(Tag tag) {
        this.content = tag.getContent();
        this.createdAt = tag.getCreatedAt();
    }
}
