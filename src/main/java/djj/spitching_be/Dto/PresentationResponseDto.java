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
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime deleted_at;
    private Integer practice_count;

    // presentation의 정보를 받아 presentationResponseDto 생성
    public PresentationResponseDto(Presentation presentation){
        this.title = presentation.getTitle();
        this.description = presentation.getDescription();
        this.practice_count = presentation.getPractice_count();
        this.created_at = presentation.getCreated_at();
        this.updated_at = presentation.getUpdated_at();
        this.deleted_at = presentation.getDeleted_at();

    }
}
