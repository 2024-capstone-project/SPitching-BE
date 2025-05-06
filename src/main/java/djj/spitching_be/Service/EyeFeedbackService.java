package djj.spitching_be.Service;

import djj.spitching_be.Domain.*;
import djj.spitching_be.Dto.EyeDto;
import djj.spitching_be.Repository.EyeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EyeFeedbackService {
    private final EyeRepository eyeRepository;

    @Transactional
    public void saveEyeFeedback(EyeDto eyeDto, User user, Presentation presentation, Practice practice){
        // DTO를 엔티티로 변환
        EyeData eyeData = EyeData.builder()
                .user(user)
                .presentation(presentation)
                .practice(practice)
                .videoUrl(eyeDto.getVideoUrl())
                .eyecontactScore(eyeDto.getEyecontactScore())
                .build();

        // 저장
        eyeRepository.save(eyeData);

        log.info("eye feedback saved for user {}, presentation {}, practice {}",
                user.getId(), presentation.getId(), practice.getId());
    }

    // EyeFeedbackService.java의 getEyeFeedbackByPracticeId 메소드
    public EyeDto getEyeFeedbackByPracticeId(Long practiceId) {
        EyeData eyeData = eyeRepository.findByPracticeId(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("No eye feedback found for practice ID: " + practiceId));

        // eyeData 엔티티를 eyeDto로 변환해서 프론트로 반환할 준비
        return convertToDto(eyeData);
    }

    // EyeData 엔티티를 eyeDto로 변환하는 메소드
    private EyeDto convertToDto(EyeData eyeData) {
        return EyeDto.builder()
                .userId(eyeData.getUser().getId())
                .presentationId(eyeData.getPresentation().getId())
                .practiceId(eyeData.getPractice().getId())
                .eyecontactScore(eyeData.getEyecontactScore())
                .videoUrl(eyeData.getVideoUrl())
                .build();
    }
}
