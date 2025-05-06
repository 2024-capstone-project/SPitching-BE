package djj.spitching_be.Controller;

import djj.spitching_be.Service.ScriptSimilarityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/v1/feedback")
@RequiredArgsConstructor
public class ScriptSimilarityController {

    private final ScriptSimilarityService scriptSimilarityService;

    @GetMapping("/practice/{practiceId}/script-similarity")
    public ResponseEntity<?> getScriptSimilarity(@PathVariable Long practiceId) {
        try {
            double similarity = scriptSimilarityService.getScriptSimilarity(practiceId);

            Map<String, Object> response = new HashMap<>();
            response.put("practiceId", practiceId);
            response.put("scriptSimilarity", similarity);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving script similarity", e);
            return ResponseEntity.internalServerError().body("Error retrieving script similarity: " + e.getMessage());
        }
    }
}
