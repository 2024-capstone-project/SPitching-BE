package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Practice;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PracticeResponseDto {
    // presentation에서 값을 가져올 때 (응답 시) presentation(직접적인 entity) 대신 앞에 서줌
    private Long id;
    private Practice.PracticeType practice_type;
    private String duration;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer practiceCount;

    private UserResponseDto user;
    private PresentationResponseDto presentation;

    //
    public PracticeResponseDto(Practice practice){
        this.practice_type = practice.getPractice_type();
        this.duration = practice.getDuration();
        this.createdAt = presentation.getCreatedAt();
        this.updatedAt = presentation.getUpdatedAt();
        this.deletedAt = presentation.getDeletedAt();
        this.presentation = new PresentationResponseDto(practice.getPresentation());
    }
}
