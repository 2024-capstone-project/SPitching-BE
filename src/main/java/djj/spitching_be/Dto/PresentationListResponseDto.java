package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PresentationListResponseDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Long id;
    private String title;
    private String description;
    private Integer practiceCount;
    private String duration;
    private List<?> slides = new ArrayList<>();
    private List<?> practices = new ArrayList<>();
    private Double totalScore;
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

        // 가장 최근 연습의 totalScore 값을 가져옴
        if (presentation.getPractices() != null && !presentation.getPractices().isEmpty()) {
            // 가장 최근 연습을 찾아서 총점 가져오기
            Optional<Practice> mostRecentPractice = presentation.getPractices().stream()
                    .max(Comparator.comparing(Practice::getCreatedAt));

            if (mostRecentPractice.isPresent()) {
                this.totalScore = mostRecentPractice.get().getTotalScore();
            } else {
                this.totalScore = null; // 연습이 없는 경우
            }
        } else {
            this.totalScore = null;
        }

        // 첫 번째 슬라이드의 이미지 URL 설정
        if (presentation.getSlides() != null && !presentation.getSlides().isEmpty()) {
            PresentationSlide firstSlide = presentation.getSlides().stream()
                    .filter(slide -> slide.getSlideNumber() == 1)
                    .findFirst()
                    .orElse(presentation.getSlides().get(0));
            this.firstSlideImageUrl = firstSlide.getImageUrl();
        } else {
            this.firstSlideImageUrl = null;
        }
    }

    public PresentationListResponseDto(Optional<Presentation> presentation){
        this.title = presentation.get().getTitle();
        this.createdAt = presentation.get().getCreatedAt();
        this.updatedAt = presentation.get().getUpdatedAt();
    }
}