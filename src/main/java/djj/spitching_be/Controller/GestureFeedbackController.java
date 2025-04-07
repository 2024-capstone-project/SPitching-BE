package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.GestureDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.UserRepository;
import djj.spitching_be.Service.GestureFeedbackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/feedback")
@RequiredArgsConstructor
public class GestureFeedbackController {

    private final GestureFeedbackService gestureFeedbackService;
    private final UserRepository userRepository;
    private final PresentationRepository presentationRepository;

    @PostMapping("/gesture")
    public ResponseEntity<String> saveGestureFeedback(@RequestBody GestureDto gestureDto) {
        log.info("Received gesture feedback from AI service: {}", gestureDto);

        try {
            // 사용자와 발표 정보 검증
            // 이 유저 아이디가 데이터베이스에 존재하는지 검증
            User user = userRepository.findById(gestureDto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID : " + gestureDto.getUserId()));

            // 이 발표 연습 아이디가 데이터베이스에 존재하는지 검증
            Presentation presentation = presentationRepository.findById(gestureDto.getPresentationId())
                    .orElseThrow(() -> new EntityNotFoundException("Presentation not found with ID: " + gestureDto.getPresentationId()));

            // 소유자의 Id와 웹훅에서 받은 id를 비교
            if (!presentation.getUser().getId().equals(user.getId())){
                return ResponseEntity.badRequest().body("User does not own this presentation");
            }

            // 제스처 피드백 저장
            gestureFeedbackService.saveGestureFeedback(gestureDto, user, presentation);

            return ResponseEntity.ok("Gesture feedback saved successfully");
        }catch (EntityNotFoundException e){
            log.error("Entity not found : {}", e.getMessage()); // what kind of msg server get? 위에서 설정한 EntityNotFoundException의 메세지
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            log.error("Error processing gesture feedback", e);
            return ResponseEntity.internalServerError().body("Error processing feedback: " + e.getMessage());
        }
    }
}
