package djj.spitching_be.Service;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Dto.PracticeRequestDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticeService {
    private final PracticeRepository practiceRepository;
    private final PresentationRepository presentationRepository;

    // 생성자 주입 방식
    public PracticeService(PresentationRepository presentationRepository, PracticeRepository practiceRepository) {
        this.presentationRepository = presentationRepository;
        this.practiceRepository = practiceRepository;
    }
    @Transactional
    public Practice createPractice(PracticeRequestDto requestDto, Presentation presentation) {
        // Practice 객체 생성 - 사용자가 입력한 practice_type과 duration 사용
        Practice practice = new Practice(requestDto, presentation);

        // 발표 연습 횟수 증가
        if (presentation.getPracticeCount() == null) {
            presentation.setPracticeCount(1);
        } else {
            presentation.setPracticeCount(presentation.getPracticeCount() + 1);
        }
        presentationRepository.save(presentation);

        // practice 저장 및 반환
        return practiceRepository.save(practice);
    }
}
