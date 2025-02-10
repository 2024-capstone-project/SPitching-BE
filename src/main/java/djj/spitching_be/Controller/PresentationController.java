package djj.spitching_be.Controller;

import djj.spitching_be.Dto.PresentationListResponseDto;
import djj.spitching_be.Dto.PresentationRequestDto;
import djj.spitching_be.Dto.PresentationResponseDto;
import djj.spitching_be.Service.PresentationService;
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
    @GetMapping
    public List<PresentationListResponseDto> getAllPresentations(){
        return presentationService.findAllPresentation();
    }

    // 발표 하나 조회
    @GetMapping("presentations/{id}")
    public PresentationResponseDto getOnePresentation(@PathVariable Long id){
        return presentationService.findOnePresentation(id);
    }

    // 발표 수정 - 제목 수정
    @PutMapping("/presentations/{id}")
    public Long updatePresentation(@PathVariable Long id, @RequestBody PresentationRequestDto requestDto){
        return presentationService.updatePresentation(id,requestDto);
    }

    // 발표 삭제
    @DeleteMapping("/presentations/{id}")
    public Long deletePresentation(@PathVariable Long id){
        return presentationService.deletePresentation(id);
    }
}
