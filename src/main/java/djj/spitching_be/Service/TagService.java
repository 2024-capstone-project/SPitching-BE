package djj.spitching_be.Service;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.Tag;
import djj.spitching_be.Dto.*;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public TagResponseDto createTag(TagRequestDto requestDto, Long slideId){
        PresentationSlide presentationSlide = presentationSlideRepository.findById(slideId)
                .orElseThrow(() -> new RuntimeException("Slide not found"));

        Tag tag = new Tag(requestDto, presentationSlide);
        tagRepository.save(tag);
        return new TagResponseDto(tag);
    }

    // 특정 슬라이드에 해당하는 모든 태그 목록 조회
    public List<TagListResponseDto> findAllTagsBySlideId(Long slideId) {
        List<Tag> tags = tagRepository.findByPresentationSlideId(slideId); // 여러 태그 가져오기
        return tags.stream()
                .map(TagListResponseDto ::new)
                .collect(Collectors.toList());
    }

    // 태그 하나 조회
    public TagResponseDto findOneTag(Long id){
        Tag tag = tagRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("조회 실패")
        );
        return new TagResponseDto(tag);
    }

    //태그 내용 수정
    @Transactional
    public String updateTag(Long id, TagRequestDto requestDto) {
        // 수정할 태그 찾기
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 태그가 존재하지 않습니다. id: " + id));

        // 태그 내용 수정
        tag.setContent(requestDto.getContent());

        // 변경된 태그 정보 반환
        return "Updated";
    }

    // 태그 삭제
    @Transactional
    public String deleteTag(Long id){
        tagRepository.deleteById(id);
        return "Deleted";
    }






}
