package djj.spitching_be.Service;

import djj.spitching_be.Domain.*;
import djj.spitching_be.Dto.*;
import djj.spitching_be.Repository.SttRepository;
import djj.spitching_be.Repository.SttTranscriptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttFeedbackService {
    private final SttRepository sttRepository;
    private final SttTranscriptRepository sttTranscriptRepository;
    private final ScriptSimilarityService scriptSimilarityService; // 추가: ScriptSimilarityService 주입
    private final TotalScoreService totalScoreService; // 추가: TotalScoreService 주입

    @Transactional
    public void saveSttFeedback(SttDto sttDto, User user, Presentation presentation, Practice practice) {
        log.info("Saving STT feedback for user {}, presentation {}, practice {}",
                user.getId(), presentation.getId(), practice.getId());

        // 1. SttData 엔티티 생성 및 기본 정보 설정
        SttData sttData = SttData.builder()
                .user(user)
                .presentation(presentation)
                .practice(practice)
                .build();

        // 2. 필러(추임새) 통계 설정
        if (sttDto.getStatisticsFiller() != null && !sttDto.getStatisticsFiller().isEmpty()) {
            SttFillerStatisticsDto fillerStats = sttDto.getStatisticsFiller().get(0);
            sttData.setFillerEo(fillerStats.getEo());
            sttData.setFillerEum(fillerStats.getEum());
            sttData.setFillerGeu(fillerStats.getGeu());
            sttData.setTotalFillerCount(fillerStats.getTotalFillerCount());
            sttData.setFillerRatio(fillerStats.getFillerRatio());
        }

        // 3. 침묵 통계 설정
        if (sttDto.getStatisticsSilence() != null && !sttDto.getStatisticsSilence().isEmpty()) {
            SttSilenceStatisticsDto silenceStats = sttDto.getStatisticsSilence().get(0);
            sttData.setSilenceRatio(silenceStats.getSilenceRatio());
            sttData.setSpeakingRatio(silenceStats.getSpeakingRatio());
            sttData.setTotalPresentationTime(silenceStats.getTotalPresentationTime());
        }

        // 4. STT 점수 설정
        if (sttDto.getSttScoreFeedback() != null && !sttDto.getSttScoreFeedback().isEmpty()) {
            sttData.setFluencyScore(sttDto.getSttScoreFeedback().get(0).getFluencyScore());
        }

        // 5. SttData 저장
        SttData savedSttData = sttRepository.save(sttData);

        // 6. 트랜스크립트 세그먼트 처리 및 저장
        if (sttDto.getTranscript() != null && !sttDto.getTranscript().isEmpty()) {
            List<SttTranscriptSegment> segments = new ArrayList<>();

            for (SttTranscriptSegmentDto segmentDto : sttDto.getTranscript()) {
                SttTranscriptSegment segment = SttTranscriptSegment.builder()
                        .sttData(savedSttData)
                        .start(segmentDto.getStart())
                        .end(segmentDto.getEnd())
                        .tag(segmentDto.getTag())
                        .result(segmentDto.getResult())
                        .build();

                segments.add(segment);
            }

            sttTranscriptRepository.saveAll(segments);
        }

        // 7. STT 데이터 저장 후 대본 유사도 계산 및 저장
        scriptSimilarityService.calculateAndSaveScriptSimilarity(sttDto);

        // 전체 점수 계산 시도
        totalScoreService.calculateTotalScoreIfAllAvailable(practice.getId());

        log.info("STT feedback saved successfully and total score calculation attempted");
    }

    // 연습 ID로 STT 피드백 조회
    public SttDto getSttFeedbackByPracticeId(Long practiceId) {
        log.info("Fetching STT feedback for practice ID: {}", practiceId);

        SttData sttData = sttRepository.findByPracticeId(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("No STT feedback found for practice ID: " + practiceId));

        // 엔티티를 DTO로 변환
        return convertToDto(sttData);
    }

    // SttData 엔티티를 SttDto로 변환
    private SttDto convertToDto(SttData sttData) {
        // 1. 필러 통계 DTO 생성
        SttFillerStatisticsDto fillerStats = SttFillerStatisticsDto.builder()
                .eo(sttData.getFillerEo())
                .eum(sttData.getFillerEum())
                .geu(sttData.getFillerGeu())
                .totalFillerCount(sttData.getTotalFillerCount())
                .fillerRatio(sttData.getFillerRatio())
                .build();

        // 2. 침묵 통계 DTO 생성
        SttSilenceStatisticsDto silenceStats = SttSilenceStatisticsDto.builder()
                .silenceRatio(sttData.getSilenceRatio())
                .speakingRatio(sttData.getSpeakingRatio())
                .totalPresentationTime(sttData.getTotalPresentationTime())
                .build();

        // 3. STT 점수 DTO 생성
        SttScoreFeedbackDto scoreDto = SttScoreFeedbackDto.builder()
                .fluencyScore(sttData.getFluencyScore())
                .build();

        // 4. 트랜스크립트 세그먼트 DTO 생성
        List<SttTranscriptSegmentDto> transcriptDtos = sttData.getTranscriptSegments().stream()
                .map(segment -> SttTranscriptSegmentDto.builder()
                        .start(segment.getStart())
                        .end(segment.getEnd())
                        .tag(segment.getTag())
                        .result(segment.getResult())
                        .build())
                .collect(Collectors.toList());

        // 5. SttDto 생성 및 반환
        return SttDto.builder()
                .userId(sttData.getUser().getId())
                .presentationId(sttData.getPresentation().getId())
                .practiceId(sttData.getPractice().getId())
                .statisticsFiller(List.of(fillerStats))
                .statisticsSilence(List.of(silenceStats))
                .sttScoreFeedback(List.of(scoreDto))
                .transcript(transcriptDtos)
                .build();
    }
}