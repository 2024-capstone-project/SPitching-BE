package djj.spitching_be.Controller;

import djj.spitching_be.Dto.*;
import djj.spitching_be.Service.PresentationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // json으로 데이터를 주고받음을 선언
@RequestMapping("/api/v1")
public class PresentationController {
    private final PresentationService presentationService;

    public PresentationController(PresentationService presentationService){
        this.presentationService = presentationService;
    }

    // 발표 생성
    @PostMapping("/presentations")
    public PresentationResponseDto createPresentation(@RequestBody PresentationRequestDto requestDto){
        PresentationResponseDto presentation = presentationService.createPresentation(requestDto);
        return presentation;
    }

    // 전체 발표 목록 조회
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
}
