package djj.spitching_be.Service;

import djj.spitching_be.Domain.GestureData;
import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.GestureDto;
import djj.spitching_be.Dto.PracticeResponseDto;
import djj.spitching_be.Repository.GestureRepository;
import jakarta.persistence.EntityNotFoundException;
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

    // GestureFeedbackService.java의 getGestureFeedbackByPracticeId 메소드
    public GestureDto getGestureFeedbackByPracticeId(Long practiceId) {
        GestureData gestureData = gestureRepository.findByPracticeId(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("No gesture feedback found for practice ID: " + practiceId));

        // GestureData 엔티티를 GestureDto로 변환해서 프론트로 반환할 준비
        return convertToDto(gestureData);
    }

    // GestureData 엔티티를 GestureDto로 변환하는 메소드
    private GestureDto convertToDto(GestureData gestureData) {
        return GestureDto.builder()
                .userId(gestureData.getUser().getId())
                .presentationId(gestureData.getPresentation().getId())
                .practiceId(gestureData.getPractice().getId())
                .gestureScore(gestureData.getGestureScore())
                .straightScore(gestureData.getStraightScore())
                .explainScore(gestureData.getExplainScore())
                .crossedScore(gestureData.getCrossedScore())
                .raisedScore(gestureData.getRaisedScore())
                .faceScore(gestureData.getFaceScore())
                .videoUrl(gestureData.getVideoUrl())
                .build();
    }

}
