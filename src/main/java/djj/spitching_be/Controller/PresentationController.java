package djj.spitching_be.Controller;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.*;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Service.PresentationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController // json으로 데이터를 주고받음을 선언
@RequestMapping("/api/v1")
public class PresentationController {
    private final PresentationService presentationService;
    private final PresentationSlideRepository slideRepository;
    public PresentationController(PresentationService presentationService, PresentationSlideRepository slideRepository){
        this.presentationService = presentationService;
        this.slideRepository = slideRepository;
    }

    // 발표 생성
    @PostMapping("/presentations")
    public ResponseEntity<Presentation> createPresentation(
            @RequestBody PresentationRequestDto requestDto,
            @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return ResponseEntity.ok(presentationService.createPresentation(requestDto, email));
    }

    // 특정 사용자가 자신의 발표 연습 목록 조회하기
    @GetMapping("/presentations/my")
    public ResponseEntity<List<Presentation>> getMyPresentations(
            @AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return ResponseEntity.ok(presentationService.getUserPresentations(email));
    }

    // 전체 발표 목록 조회 - 삭제 예정
    @GetMapping("presentations/list")
    public List<PresentationListResponseDto> getAllPresentations(){
        return presentationService.findAllPresentation();
    }

    // 발표 하나 조회
    @GetMapping("presentations/{id}")
    public PresentationResponseDto getOnePresentation(@PathVariable Long id){
        return presentationService.findOnePresentation(id);
    }

    // 발표 수정 - 제목 수정
    @PatchMapping("/presentations/{id}")
    public ResponseEntity<MessageResponseDto> updatePresentation(@PathVariable Long id, @RequestBody PresentationTitleUpdateRequestDto requestDto) {
        String result = presentationService.updatePresentation(id, requestDto);  // String으로 받음
        return ResponseEntity.ok(new MessageResponseDto(result));
    }

    // 발표 삭제
    @DeleteMapping("/presentations/{id}")
    public ResponseEntity<MessageResponseDto> deletePresentation(@PathVariable Long id){
        String result = presentationService.deletePresentation(id);
        return ResponseEntity.ok(new MessageResponseDto(result));
    }

    // Pdf 업로드
    @PostMapping("/presentations/{id}/upload")
    public ResponseEntity<?> uploadPdf(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            List<PresentationSlide> slides = presentationService.uploadAndConvertPdf(id, file);
            List<PresentationSlideDto> slideDtos = slides.stream()
                    .map(PresentationSlideDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(slideDtos);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("PDF 변환 실패: " + e.getMessage());
        }
    }

    // 특정 발표 연습의 슬라이드 조회
    @GetMapping("/presentations/{id}/slides")
    public List<PresentationSlideDto> getSlides(@PathVariable Long id) {
        return slideRepository.findByPresentationId(id)
                .stream()
                .map(PresentationSlideDto::new)
                .collect(Collectors.toList());
    }
}
