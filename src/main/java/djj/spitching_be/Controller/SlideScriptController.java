package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.MessageResponseDto;
import djj.spitching_be.Dto.SlideScriptUpdateDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Dto.ScriptUpdateRequestDto;
import djj.spitching_be.Service.PresentationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class SlideScriptController {
    private final PresentationSlideRepository slideRepository;
    private final PresentationRepository presentationRepository;
    private final PresentationService presentationService;

    public SlideScriptController(PresentationSlideRepository slideRepository,
                                 PresentationRepository presentationRepository,
                                 PresentationService presentationService) {
        this.slideRepository = slideRepository;
        this.presentationRepository = presentationRepository;
        this.presentationService = presentationService;
    }

    // 발표 ID와 슬라이드 번호로 대본 업데이트
    @PutMapping("/presentations/{presentationId}/slides/{slideNumber}/script")
    public ResponseEntity<?> updateScript(
            @PathVariable Long presentationId,
            @PathVariable Integer slideNumber,
            @RequestBody ScriptUpdateRequestDto requestDto,
            @AuthenticationPrincipal OAuth2User principal) {

        // 현재 로그인한 사용자 이메일 가져오기
        String email = principal.getAttribute("email");

        // 먼저 발표가 존재하는지, 그리고 현재 사용자의 발표인지 확인
        Optional<Presentation> presentationOpt = presentationRepository.findById(presentationId);

        if (presentationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Presentation presentation = presentationOpt.get();

        // 현재 사용자의 발표인지 확인 (보안)
        if (!presentation.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body("이 발표 자료에 대한 접근 권한이 없습니다.");
        }

        // 해당 발표의 특정 슬라이드 번호 찾기
        Optional<PresentationSlide> slideOpt = slideRepository.findByPresentationIdAndSlideNumber(
                presentationId, slideNumber);

        if (slideOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PresentationSlide slide = slideOpt.get();
        slide.setScript(requestDto.getScript());
        slideRepository.save(slide);

        return ResponseEntity.ok("대본이 저장되었습니다.");
    }

    // 여러 슬라이드의 대본을 한 번에 업데이트하는 새로운 메서드
    // 발표의 모든 슬라이드 스크립트 한 번에 업데이트
    @PutMapping("/presentations/{id}/slides/script")
    public ResponseEntity<MessageResponseDto> updateSlidesScripts(
            @PathVariable Long id,
            @RequestBody List<SlideScriptUpdateDto> scriptUpdateDtos,
            @AuthenticationPrincipal OAuth2User principal) {

        // 사용자 인증 확인 (필요시)
        String email = principal.getAttribute("email");

        // 서비스 메소드 호출하여 스크립트 업데이트
        String result = presentationService.updateSlidesScripts(id, scriptUpdateDtos, email);

        return ResponseEntity.ok(new MessageResponseDto(result));
    }
}