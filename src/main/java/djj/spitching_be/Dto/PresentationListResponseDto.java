package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Presentation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PresentationListResponseDto {
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private Integer practiceCount;

    // entity -> dto
    public PresentationListResponseDto(Presentation presentation){
        this.title = presentation.getTitle();
        this.description = presentation.getDescription();
        this.practiceCount = presentation.getPracticeCount(); // 추가
        this.createdAt = presentation.getCreatedAt();
        this.updatedAt = presentation.getUpdatedAt();
        this.deletedAt = presentation.getDeletedAt();
    }

    public PresentationListResponseDto(Optional<Presentation> presentation){
        this.title = presentation.get().getTitle();
        this.createdAt = presentation.get().getUpdatedAt();
        this.updatedAt = presentation.get().getUpdatedAt();
    }


}
