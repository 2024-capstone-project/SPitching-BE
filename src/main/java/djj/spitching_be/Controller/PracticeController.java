package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Dto.PracticeRequestDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Service.PracticeService;
import djj.spitching_be.Service.PresentationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController // json으로 데이터를 주고받음을 선언
@RequestMapping("/api/v1")
public class PracticeController {

    private PresentationRepository presentationRepository;
    private PresentationService presentationService;
    private PracticeService practiceService;
    @PostMapping("/start")
    public ResponseEntity<?> startPractice(@RequestBody PracticeRequestDto requestDto,
                                           @RequestParam Long presentationId){
        try{
            // 발표 정보 조회
            Presentation presentation = presentationRepository.findById(presentationId)
                    .orElseThrow(() -> new EntityNotFoundException("presentation not found: " + presentationId));

            // 사용자가 입력한 practice_type과 duration 으로 practice 객체 생성
            Practice practice = practiceService.createPractice(requestDto, presentation);

            // 생성된 Practice ID를 응답에 포함
            Map<String, Object> response = new HashMap<>();
            response.put("practiceId", practice.getId());
            response.put("message", "Practice session started successfully");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error starting practice: ", e);
            return ResponseEntity.internalServerError().body("Error starting practice: " + e.getMessage());
        }

        }
    }
