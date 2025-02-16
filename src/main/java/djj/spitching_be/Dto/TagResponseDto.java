package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.Tag;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TagResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;

    public TagResponseDto(Tag tag){
        this.id = tag.getId();
        this.content = tag.getContent();
        this.createdAt = tag.getCreatedAt();
    }
}
