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
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime deleted_at;
    private Integer practice_count;

    // entity -> dto
    public PresentationListResponseDto(Presentation presentation){
        this.title = presentation.getTitle();
        this.description = presentation.getDescription();
        this.practice_count = presentation.getPractice_count(); // 추가
        this.created_at = presentation.getCreated_at();
        this.updated_at = presentation.getUpdated_at();
        this.deleted_at = presentation.getDeleted_at();
    }

    public PresentationListResponseDto(Optional<Presentation> presentation){
        this.title = presentation.get().getTitle();
        this.created_at = presentation.get().getUpdated_at();
        this.updated_at = presentation.get().getUpdated_at();
    }


}
