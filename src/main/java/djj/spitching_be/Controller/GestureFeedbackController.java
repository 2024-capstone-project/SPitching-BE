package djj.spitching_be.Controller;

import djj.spitching_be.Dto.GestureDto;
import djj.spitching_be.Service.GestureFeedbackService;
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

    @PostMapping("/gesture")
    public ResponseEntity<String> saveGestureFeedback(@RequestBody GestureDto gestureDto) {
        log.info("Received gesture feedback from AI service: {}", gestureDto);

        try {
            gestureFeedbackService.processGestureFeedback(gestureDto);
            return ResponseEntity.ok("Gesture feedback saved");
        } catch (Exception e) {
            log.error("Error processing gesture feedback", e);
            return ResponseEntity.status(500).body("Error processing feedback: " + e.getMessage());
        }
    }
}
