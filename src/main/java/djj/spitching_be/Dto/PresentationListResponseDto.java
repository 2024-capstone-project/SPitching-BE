package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PresentationListResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer practiceCount;
    private String duration;
    private List<?> slides = new ArrayList<>();
    private List<?> practices = new ArrayList<>();
    private Integer totalScore;
    private String firstSlideImageUrl;

    // entity -> dto
    public PresentationListResponseDto(Presentation presentation){
        this.createdAt = presentation.getCreatedAt();
        this.updatedAt = presentation.getUpdatedAt();
        this.deletedAt = presentation.getDeletedAt();
        this.id = presentation.getId();
        this.title = presentation.getTitle();
        this.description = presentation.getDescription();
        this.practiceCount = presentation.getPracticeCount();
        this.duration = presentation.getDuration();
        this.slides = new ArrayList<>(); // 빈 리스트로 초기화
        this.practices = new ArrayList<>(); // 빈 리스트로 초기화
        this.totalScore = 98; // 예시 값 또는 계산 로직 필요

        // 첫 번째 슬라이드의 이미지 URL 설정
        if (presentation.getSlides() != null && !presentation.getSlides().isEmpty()) {
            PresentationSlide firstSlide = presentation.getSlides().stream()
                    .filter(slide -> slide.getSlideNumber() == 1)
                    .findFirst()
                    .orElse(presentation.getSlides().get(0));
            this.firstSlideImageUrl = firstSlide.getImageUrl();
        } else {
            this.firstSlideImageUrl = null; // 또는 기본 이미지 URL
        }
    }

    public PresentationListResponseDto(Optional<Presentation> presentation){
        this.title = presentation.get().getTitle();
        this.createdAt = presentation.get().getCreatedAt();
        this.updatedAt = presentation.get().getUpdatedAt();
    }


}
