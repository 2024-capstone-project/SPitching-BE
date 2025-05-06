package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.User;
import djj.spitching_be.Dto.EyeDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.UserRepository;
import djj.spitching_be.Service.EyeFeedbackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("api/v1/feedback")
@RequiredArgsConstructor
public class EyeFeedbackController {
    private final EyeFeedbackService eyeFeedbackService;
    private final UserRepository userRepository;
    private final PresentationRepository presentationRepository;
    private final PracticeRepository practiceRepository;

    @PostMapping("/eye")
    public ResponseEntity<String> saveEyeFeedback(@RequestBody EyeDto eyeDto){
        log.info("Received eye feedback from AI service: {}", eyeDto);

        try{
            // 사용자와 발표 정보 검증
            // 이 유저 아이디가 데이터베이스에 존재하는지 검증
            User user = userRepository.findById(eyeDto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with ID : " + eyeDto.getUserId()));

            // 이 발표 연습 아이디가 데이터베이스에 존재하는지 검증
            Presentation presentation = presentationRepository.findById(eyeDto.getPresentationId())
                    .orElseThrow(() -> new EntityNotFoundException("Presentation not found with ID: " + eyeDto.getPresentationId()));

            Practice practice = practiceRepository.findById(eyeDto.getPracticeId())
                    .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + eyeDto.getPracticeId()));


            // 소유자의 Id와 웹훅에서 받은 id를 비교
            if (!presentation.getUser().getId().equals(user.getId())){
                return ResponseEntity.badRequest().body("User does not own this presentation");
            }

            // 제스처 피드백 저장
            eyeFeedbackService.saveEyeFeedback(eyeDto, user, presentation, practice);

            return ResponseEntity.ok("eye feedback saved successfully");
        }catch (EntityNotFoundException e){
            log.error("Entity not found : {}", e.getMessage()); // what kind of msg server get? 위에서 설정한 EntityNotFoundException의 메세지
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch (Exception e){
            log.error("Error processing eye feedback", e);
            return ResponseEntity.internalServerError().body("Error processing feedback: " + e.getMessage());
        }
    }

    @GetMapping("/practice/{practiceId}/eye")
    public ResponseEntity<?> getEyeFeedbackByPractice(@PathVariable Long practiceId){
        try {
            // 연습 존재 여부 확인
            if (!practiceRepository.existsById(practiceId)) {
                return ResponseEntity.badRequest().body("Practice not found with ID: " + practiceId);
            }

            EyeDto eyeDto = eyeFeedbackService.getEyeFeedbackByPracticeId(practiceId);
            return ResponseEntity.ok(eyeDto);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving eye feedback", e);
            return ResponseEntity.internalServerError().body("Error retrieving feedback: " + e.getMessage());
        }
    }
    }
