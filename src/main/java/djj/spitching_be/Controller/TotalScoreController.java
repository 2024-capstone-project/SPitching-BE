package djj.spitching_be.Controller;

import djj.spitching_be.Service.TotalScoreService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/feedback")
@RequiredArgsConstructor
public class TotalScoreController {

    private final TotalScoreService totalScoreService;

    /**
     * 특정 연습의 전체 점수 상세 정보를 조회합니다.
     */
    @GetMapping("/practice/{practiceId}/score-details")
    public ResponseEntity<?> getTotalScoreDetails(@PathVariable Long practiceId) {
        try {
            Map<String, Object> scoreDetails = totalScoreService.getTotalScoreDetails(practiceId);
            return ResponseEntity.ok(scoreDetails);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving total score details", e);
            return ResponseEntity.internalServerError().body("Error retrieving score details: " + e.getMessage());
        }
    }

    /**
     * 특정 연습의 전체 점수만 조회합니다.
     */
    @GetMapping("/practice/{practiceId}/total-score")
    public ResponseEntity<?> getTotalScore(@PathVariable Long practiceId) {
        try {
            Double totalScore = totalScoreService.getTotalScore(practiceId);

            return ResponseEntity.ok(Map.of(
                    "practiceId", practiceId,
                    "totalScore", totalScore
            ));
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving total score", e);
            return ResponseEntity.internalServerError().body("Error retrieving total score: " + e.getMessage());
        }
    }

    /**
     * 특정 연습의 전체 점수를 강제로 계산하고 저장합니다.
     */
    @PostMapping("/practice/{practiceId}/calculate-score")
    public ResponseEntity<?> calculateTotalScore(@PathVariable Long practiceId) {
        try {
            Map<String, Object> result = totalScoreService.calculateAndSaveTotalScore(practiceId);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error calculating total score", e);
            return ResponseEntity.internalServerError().body("Error calculating total score: " + e.getMessage());
        }
    }
}

