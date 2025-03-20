package djj.spitching_be.Controller;

import djj.spitching_be.Dto.ChatMessageDto;
import djj.spitching_be.Dto.QASessionDto;
import djj.spitching_be.Service.QAChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/presentations/{presentationId}/qa-session")
@RequiredArgsConstructor
public class QAChatbotController {

    private final QAChatbotService qaChatbotService;

    @PostMapping("/start")
    public ResponseEntity<QASessionDto> startQASession(@PathVariable Long presentationId) {
        QASessionDto session = qaChatbotService.startQASession(presentationId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/generate-question")
    public ResponseEntity<ChatMessageDto> generateQuestion(
            @PathVariable Long presentationId,
            @RequestBody ChatMessageDto userMessage) {
        ChatMessageDto botResponse = qaChatbotService.generateQuestion(presentationId, userMessage.getContent());
        return ResponseEntity.ok(botResponse);
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatMessageDto> chat(
            @PathVariable Long presentationId,
            @RequestBody ChatMessageDto userMessage) {
        ChatMessageDto botResponse = qaChatbotService.processChatMessage(presentationId, userMessage);
        return ResponseEntity.ok(botResponse);
    }
}
