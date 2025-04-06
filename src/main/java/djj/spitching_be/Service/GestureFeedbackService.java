package djj.spitching_be.Service;

import djj.spitching_be.Domain.GestureData;
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
    public void processGestureFeedback(GestureDto gestureDto){
        // 사용자 ID는 나중에 필요할 경우 매개변수로 추가 가능
        GestureData gestureData = convertToGestureData(gestureDto);
        gestureRepository.save(gestureData);
        log.info("Gesture data saved to db : {}", gestureData);
    }

    private GestureData convertToGestureData(GestureDto gestureDto){
        return GestureData.builder()
                .gestureScore(gestureDto.getGestureScore())
                .straightScore(gestureDto.getStraight_score())
                .explainScore(gestureDto.getExplain_score())
                .crossedScore(gestureDto.getCrossed_score())
                .raisedScore(gestureDto.getRaised_score())
                .faceScore(gestureDto.getFace_score())
                .videoUrl(gestureDto.getVideoUrl())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
