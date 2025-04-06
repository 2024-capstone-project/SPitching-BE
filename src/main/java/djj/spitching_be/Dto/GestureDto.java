package djj.spitching_be.Dto;

import lombok.Data;

@Data
public class GestureDto {
    private int gestureScore;
    private int straight_score;
    private int explain_score;
    private int crossed_score;
    private int raised_score;
    private int face_score;
    private String videoUrl;
}
