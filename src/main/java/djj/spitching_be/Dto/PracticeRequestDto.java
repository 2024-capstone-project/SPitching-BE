package djj.spitching_be.Dto;

import djj.spitching_be.Domain.Practice;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PracticeRequestDto {

    private Practice.PracticeType practice_type;

    // private String duration;
}
