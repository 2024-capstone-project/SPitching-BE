package djj.spitching_be.Dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class EyeContactDTO {
    private Integer eyecontactScore;
    private String videoUrl;

    public EyeContactDTO() {
    }

    public Integer getEyecontactScore() {
        return eyecontactScore;
    }

    public void setEyecontactScore(Integer eyecontactScore) {
        this.eyecontactScore = eyecontactScore;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}