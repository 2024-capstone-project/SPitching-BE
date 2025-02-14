package djj.spitching_be.Service;

import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.Tag;
import djj.spitching_be.Dto.TagListResponseDto;
import djj.spitching_be.Dto.TagRequestDto;
import djj.spitching_be.Dto.TagResponseDto;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private final PresentationSlideRepository presentationSlideRepository;

    public TagService(TagRepository tagRepository, PresentationSlideRepository presentationSlideRepository){
        this.tagRepository = tagRepository;
        this.presentationSlideRepository = presentationSlideRepository;
    }

    // 태그 생성
    public TagResponseDto createTag(TagRequestDto requestDto){
        PresentationSlide presentationSlide = presentationSlideRepository.findById(requestDto.getSlideId())
                .orElseThrow(() -> new RuntimeException("Slide not found"));

        Tag tag = new Tag(requestDto, presentationSlide);
        tagRepository.save(tag);
        return new TagResponseDto(tag);
    }

    // 특정 슬라이드에 해당하는 모든 태그 가져오기
    public List<TagListResponseDto> findAllTagsBySlideId(Long slideId) {
        List<Tag> tags = tagRepository.findByPresentationSlideId(slideId); // 여러 태그 가져오기
        return tags.stream()
                .map(TagListResponseDto ::new)
                .collect(Collectors.toList());
    }




}
