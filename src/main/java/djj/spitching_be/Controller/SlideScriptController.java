package djj.spitching_be.Controller;

import djj.spitching_be.Repository.PresentationSlideRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class SlideScriptController {
    private final PresentationSlideRepository slideRepository;

    public SlideScriptController(PresentationSlideRepository slideRepository){
        this.slideRepository = slideRepository;
    }

    @PutMapping("/slides/{id}/script")
    public ResponseEntity<?> updateScript(@PathVariable Long id, @RequestBody String script){
        return slideRepository.findById(id)
                .map(slide -> {
                    slide.setScript(script);
                    slideRepository.save(slide);
                    return ResponseEntity.ok("대본이 저장되었습니다.");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
