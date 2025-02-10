package djj.spitching_be.Service;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Dto.PresentationListResponseDto;
import djj.spitching_be.Dto.PresentationRequestDto;
import djj.spitching_be.Dto.PresentationResponseDto;
import djj.spitching_be.Repository.PresentationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PresentationService {
    private final PresentationRepository presentationRepository;

    // 발표 생성
    public PresentationResponseDto createPresentation(PresentationRequestDto requestDto){
        Presentation presentation = new Presentation(requestDto);
        presentationRepository.save(presentation);
        return new PresentationResponseDto(presentation);
    }

    // 모든 발표 가져오기
    public List<PresentationListResponseDto> findAllPresentation(){
        try{
            List<Presentation> presentationList = presentationRepository.findAll();
            List<PresentationListResponseDto> responseDtoList = new ArrayList<>();
            for (Presentation presentation : presentationList) {
                responseDtoList.add(new PresentationListResponseDto(presentation));
            }
            return responseDtoList;
        }catch (Exception e){
        }
        return null;
    }

    // 발표 하나 가져오기
    public PresentationResponseDto findOnePresentation(Long id){
        Presentation presentation = presentationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("조회 실패")
        );
        return new PresentationResponseDto(presentation);
    }

    // 발표 수정 - 제목 수정
    @Transactional
    public Long updatePresentation(Long id, PresentationRequestDto requestDto){
        Presentation presentation = presentationRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 아이디가 존재하지 않습니다.")
        );
        presentation.update(requestDto);
        return presentation.getId();
    }

    // 발표 삭제
    @Transactional
    public Long deletePresentation(Long id){
        presentationRepository.deleteById(id);
        return id;
    }
}
