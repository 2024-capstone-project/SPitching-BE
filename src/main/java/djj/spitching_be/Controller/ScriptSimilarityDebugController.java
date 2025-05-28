package djj.spitching_be.Controller;

import djj.spitching_be.Service.ScriptSimilarityService;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.SttRepository;
import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.SttData;
import djj.spitching_be.Dto.SttDto;
import djj.spitching_be.Dto.SttTranscriptSegmentDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("api/v1/debug")
@RequiredArgsConstructor
public class ScriptSimilarityDebugController {

    private final ScriptSimilarityService scriptSimilarityService;
    private final PracticeRepository practiceRepository;
    private final SttRepository sttRepository;

    /**
     * 기존 STT 데이터에 대해 유사도 강제 재계산
     */
    @PostMapping("/practice/{practiceId}/force-recalculate")
    public ResponseEntity<?> forceRecalculateSimilarity(@PathVariable Long practiceId) {
        try {
            Practice practice = practiceRepository.findById(practiceId)
                    .orElseThrow(() -> new EntityNotFoundException("Practice not found"));

            // 기존 유사도 값
            Double oldSimilarity = practice.getScriptSimilarity();

            // STT 데이터 조회
            Optional<SttData> sttDataOpt = sttRepository.findByPracticeId(practiceId);
            if (sttDataOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("No STT data found for this practice");
            }

            SttData sttData = sttDataOpt.get();

            // SttTranscriptSegmentDto 리스트로 변환
            List<SttTranscriptSegmentDto> transcriptList = sttData.getTranscriptSegments().stream()
                    .map(segment -> SttTranscriptSegmentDto.builder()
                            .start(segment.getStart())
                            .end(segment.getEnd())
                            .tag(segment.getTag())
                            .result(segment.getResult())
                            .build())
                    .collect(Collectors.toList());

            // SttDto 생성 (ScriptSimilarityService의 기존 로직 사용)
            SttDto sttDto = SttDto.builder()
                    .practiceId(practiceId)
                    .presentationId(practice.getPresentation().getId())
                    .userId(sttData.getUser().getId())
                    .transcript(transcriptList)
                    .build();

            // 유사도 재계산 (기존 Service 로직 사용)
            double newSimilarity = scriptSimilarityService.calculateAndSaveScriptSimilarity(sttDto);

            Map<String, Object> response = new HashMap<>();
            response.put("practiceId", practiceId);
            response.put("oldSimilarity", oldSimilarity);
            response.put("newSimilarity", newSimilarity);
            response.put("success", true);

            log.info("Force recalculated similarity for practice {}: {} -> {}",
                    practiceId, oldSimilarity, newSimilarity);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error force recalculating similarity", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Practice 상태 확인 (디버깅용)
     */
    @GetMapping("/practice/{practiceId}/status")
    public ResponseEntity<?> getPracticeStatus(@PathVariable Long practiceId) {
        try {
            Practice practice = practiceRepository.findById(practiceId)
                    .orElseThrow(() -> new EntityNotFoundException("Practice not found"));

            Optional<SttData> sttData = sttRepository.findByPracticeId(practiceId);

            Map<String, Object> response = new HashMap<>();
            response.put("practiceId", practiceId);
            response.put("presentationId", practice.getPresentation().getId());
            response.put("currentSimilarity", practice.getScriptSimilarity());
            response.put("hasSttData", sttData.isPresent());

            if (sttData.isPresent()) {
                response.put("sttSegmentCount", sttData.get().getTranscriptSegments().size());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting practice status", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}