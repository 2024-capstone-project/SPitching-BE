package djj.spitching_be.Controller;

import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.TagRequestDto;
import djj.spitching_be.Dto.TagResponseDto;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.TagRepository;
import djj.spitching_be.Service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class TagController {
    private final TagService tagService;
    private final TagRepository tagRepository;

    @Autowired
    private PresentationSlideRepository presentationSlideRepository;  // PresentationSlide를 조회할 리포지토리

    public TagController(TagService tagService, TagRepository tagRepository){
        this.tagRepository = tagRepository;
        this.tagService = tagService;
    }

    // 태그 생성
    @PostMapping("/tags")
    public TagResponseDto createTag(@RequestBody TagRequestDto requestDto){
        return tagService.createTag(requestDto);
    }
}
