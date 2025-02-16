package djj.spitching_be.Dto;

import djj.spitching_be.Domain.PresentationSlide;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PresentationSlideDto {
    private Long id;
    private Integer slideNumber;
    private String imageUrl;
    private String script;
    private LocalDateTime createdAt;

    public PresentationSlideDto(PresentationSlide slide) {
        this.id = slide.getId();
        this.slideNumber = slide.getSlideNumber();
        this.imageUrl = slide.getImageUrl();
        this.script = slide.getScript();
        this.createdAt = slide.getCreatedAt();
    }
}
