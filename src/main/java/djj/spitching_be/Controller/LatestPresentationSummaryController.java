package djj.spitching_be.Controller;

import djj.spitching_be.Dto.LatestPresentationSummaryDto;
import djj.spitching_be.Service.LatestPresentationSummaryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

//import javax.servlet.http.HttpSession;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LatestPresentationSummaryController {

    private final LatestPresentationSummaryService latestPresentationSummaryService;

    /**
     * 현재 로그인한 사용자의 가장 최근 발표 요약 정보를 조회합니다.
     * JSESSIONID를 사용하여 사용자 인증을 수행합니다.
     */
    @GetMapping("/home/summary")
    public ResponseEntity<?> getLatestPresentationSummary(HttpSession session) {
        try {
            // 세션에서 사용자 ID 추출
            Long userId = (Long) session.getAttribute("userId");

            // 세션에 사용자 정보가 없는 경우
            if (userId == null) {
                log.warn("No user ID in session");
                return ResponseEntity.status(401).body("Unauthorized: No user ID in session");
            }

            log.info("Getting latest presentation summary for user ID: {}", userId);

            // 최근 발표 요약 정보 조회
            LatestPresentationSummaryDto summary = latestPresentationSummaryService.getLatestPresentationSummary(userId);

            return ResponseEntity.ok(summary);
        } catch (NoSuchElementException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving latest presentation summary", e);
            return ResponseEntity.internalServerError().body("Error retrieving presentation summary: " + e.getMessage());
        }
    }
}