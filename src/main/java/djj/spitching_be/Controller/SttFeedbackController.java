package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.SttDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.UserRepository;
import djj.spitching_be.Service.SttFeedbackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/feedback")
@RequiredArgsConstructor
public class SttFeedbackController {
    private final SttFeedbackService sttFeedbackService;
    private final UserRepository userRepository;
    private final PresentationRepository presentationRepository;
    private final PracticeRepository practiceRepository;

    @PostMapping("/stt")
    public ResponseEntity<String> saveSttFeedback(@RequestBody SttDto sttDto) {
        log.info("Received STT feedback from AI service: {}", sttDto);

        try {
            // 1. 사용자 검증
            User user = userRepository.findById(sttDto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + sttDto.getUserId()));

            // 2. 발표 검증
            Presentation presentation = presentationRepository.findById(sttDto.getPresentationId())
                    .orElseThrow(() -> new EntityNotFoundException("Presentation not found with ID: " + sttDto.getPresentationId()));

            // 3. 연습 검증
            Practice practice = practiceRepository.findById(sttDto.getPracticeId())
                    .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + sttDto.getPracticeId()));

            // 4. 발표 소유자 확인
            if (!presentation.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("User does not own this presentation");
            }

            // 5. STT 피드백 저장
            sttFeedbackService.saveSttFeedback(sttDto, user, presentation, practice);

            return ResponseEntity.ok("STT feedback saved successfully");
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error processing STT feedback", e);
            return ResponseEntity.internalServerError().body("Error processing feedback: " + e.getMessage());
        }
    }

    @GetMapping("/practice/{practiceId}/stt")
    public ResponseEntity<?> getSttFeedbackByPractice(@PathVariable Long practiceId) {
        try {
            // 연습 존재 여부 확인
            if (!practiceRepository.existsById(practiceId)) {
                return ResponseEntity.badRequest().body("Practice not found with ID: " + practiceId);
            }

            SttDto sttDto = sttFeedbackService.getSttFeedbackByPracticeId(practiceId);
            return ResponseEntity.ok(sttDto);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving STT feedback", e);
            return ResponseEntity.internalServerError().body("Error retrieving feedback: " + e.getMessage());
        }
    }
}