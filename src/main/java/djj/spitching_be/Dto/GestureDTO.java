package djj.spitching_be.Dto;

public class GestureDTO {
    private Integer gestureScore;
    private Integer straight_score;
    private Integer explain_score;
    private Integer crossed_score;
    private Integer raised_score;
    private Integer face_score;
    private String videoUrl;

    public GestureDTO() {
    }

    public Integer getGestureScore() {
        return gestureScore;
    }

    public void setGestureScore(Integer gestureScore) {
        this.gestureScore = gestureScore;
    }

    public Integer getStraight_score() {
        return straight_score;
    }

    public void setStraight_score(Integer straight_score) {
        this.straight_score = straight_score;
    }

    public Integer getExplain_score() {
        return explain_score;
    }

    public void setExplain_score(Integer explain_score) {
        this.explain_score = explain_score;
    }

    public Integer getCrossed_score() {
        return crossed_score;
    }

    public void setCrossed_score(Integer crossed_score) {
        this.crossed_score = crossed_score;
    }

    public Integer getRaised_score() {
        return raised_score;
    }

    public void setRaised_score(Integer raised_score) {
        this.raised_score = raised_score;
    }

    public Integer getFace_score() {
        return face_score;
    }

    public void setFace_score(Integer face_score) {
        this.face_score = face_score;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
