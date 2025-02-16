package djj.spitching_be.Controller;

import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.MessageResponseDto;
import djj.spitching_be.Dto.TagListResponseDto;
import djj.spitching_be.Dto.TagRequestDto;
import djj.spitching_be.Dto.TagResponseDto;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.TagRepository;
import djj.spitching_be.Service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public TagResponseDto createTag(@RequestBody TagRequestDto requestDto, @RequestParam Long slideId) {
        // slideId로 슬라이드 조회
        return tagService.createTag(requestDto, slideId);
    }

    // 특정 슬라이드에 달린 태그 전체 목록 조회
    @GetMapping("/tags")
    public List<TagListResponseDto> getAllTags(@RequestParam Long slideId){
        return tagService.findAllTagsBySlideId(slideId);
    }

    // 태그 하나 조회
    @GetMapping("/tags/{id}")
    public TagResponseDto getOneTag(@PathVariable Long id){
        return tagService.findOneTag(id);
    }

    // 태그 내용 수정
    @PatchMapping("/tags/{id}")
    public ResponseEntity<MessageResponseDto> updateTag(@PathVariable Long id, @RequestBody TagRequestDto requestDto){
        String result = tagService.updateTag(id, requestDto);
        return ResponseEntity.ok(new MessageResponseDto(result));
    }

    // 태그 삭제
    @DeleteMapping("/tags/{id}")
    public ResponseEntity<MessageResponseDto> deleteTag(@PathVariable Long id){
        String result = tagService.deleteTag(id);
        return ResponseEntity.ok(new MessageResponseDto(result));
    }

}
