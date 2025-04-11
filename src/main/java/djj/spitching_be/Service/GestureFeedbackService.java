package djj.spitching_be.Service;

import djj.spitching_be.Domain.GestureData;
import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.GestureDto;
import djj.spitching_be.Repository.GestureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GestureFeedbackService {
    private final GestureRepository gestureRepository;

    @Transactional
    public void saveGestureFeedback(GestureDto gestureDto, User user, Presentation presentation, Practice practice){
        // DTO를 엔티티로 변환
        GestureData gestureData = GestureData.builder()
                .user(user)
                .presentation(presentation)
                .practice(practice)
                .gestureScore(gestureDto.getGestureScore())
                .straightScore(gestureDto.getStraightScore())
                .explainScore(gestureDto.getExplainScore())
                .crossedScore(gestureDto.getCrossedScore())
                .raisedScore(gestureDto.getRaisedScore())
                .faceScore(gestureDto.getFaceScore())
                .videoUrl(gestureDto.getVideoUrl())
                .build();

        // 저장
        gestureRepository.save(gestureData);

        log.info("Gesture feedback saved for user {}, presentation {}, practice {}",
                user.getId(), presentation.getId(), practice.getId());
    }
}
