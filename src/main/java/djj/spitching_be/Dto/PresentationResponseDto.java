package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Presentation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class PresentationResponseDto {

    // presentation에서 값을 가져올 때 (응답 시) presentation(직접적인 entity) 대신 앞에 서줌
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer practiceCount;

    // presentation의 정보를 받아 presentationResponseDto 생성
    public PresentationResponseDto(Presentation presentation){
        this.title = presentation.getTitle();
        this.description = presentation.getDescription();
        this.practiceCount = presentation.getPracticeCount();
        this.createdAt = presentation.getCreatedAt();
        this.updatedAt = presentation.getUpdatedAt();
        this.deletedAt = presentation.getDeletedAt();

    }
}
