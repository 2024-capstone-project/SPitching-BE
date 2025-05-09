package djj.spitching_be.Service;

import djj.spitching_be.Domain.*;
import djj.spitching_be.Dto.*;
import djj.spitching_be.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SttFeedbackService {

    private final SttRepository sttRepository;
    private final SttTranscriptRepository sttTranscriptRepository;
    private final PracticeRepository practiceRepository;
    private final ScriptSimilarityService scriptSimilarityService;
    private final TotalScoreService totalScoreService;

    @Transactional
    public void saveSttFeedback(SttDto sttDto, User user, Presentation presentation, Practice practice) {
        log.info("Saving STT feedback for user {}, presentation {}, practice {}",
                user.getId(), presentation.getId(), practice.getId());

        // 기존 데이터가 있는지 확인
        Optional<SttData> existingSttData = sttRepository.findByPracticeId(practice.getId());

        SttData sttData;
        if (existingSttData.isPresent()) {
            // 기존 데이터가 있으면 업데이트
            sttData = existingSttData.get();
            // 기존 트랜스크립트 세그먼트 삭제 (orphanRemoval=true로 설정된 경우 자동 삭제됨)
            sttData.getTranscriptSegments().clear();
        } else {
            // 새 데이터 생성
            sttData = SttData.builder()
                    .user(user)
                    .presentation(presentation)
                    .practice(practice)
                    .build();
        }

        // 필러(추임새) 통계 설정
        if (sttDto.getStatisticsFiller() != null && !sttDto.getStatisticsFiller().isEmpty()) {
            SttFillerStatisticsDto fillerStats = sttDto.getStatisticsFiller().get(0);
            sttData.setFillerEo(fillerStats.getEo());
            sttData.setFillerEum(fillerStats.getEum());
            sttData.setFillerGeu(fillerStats.getGeu());
            sttData.setTotalFillerCount(fillerStats.getTotalFillerCount());
            sttData.setFillerRatio(fillerStats.getFillerRatio());
        }

        // 침묵 통계 설정
        if (sttDto.getStatisticsSilence() != null && !sttDto.getStatisticsSilence().isEmpty()) {
            SttSilenceStatisticsDto silenceStats = sttDto.getStatisticsSilence().get(0);
            sttData.setSilenceRatio(silenceStats.getSilenceRatio());
            sttData.setSpeakingRatio(silenceStats.getSpeakingRatio());
            sttData.setTotalPresentationTime(silenceStats.getTotalPresentationTime());
        }

        // 유창성 점수 설정 (수정: 루트 레벨에서 직접 가져옴)
        if (sttDto.getFluencyScore() != null) {
            sttData.setFluencyScore(sttDto.getFluencyScore());
        }

        // 저장
        SttData savedSttData = sttRepository.save(sttData);

        // 트랜스크립트 세그먼트 처리 및 저장
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

        // 대본 유사도 계산 및 저장
        scriptSimilarityService.calculateAndSaveScriptSimilarity(sttDto);

        // 전체 점수 계산 시도
        totalScoreService.calculateTotalScoreIfAllAvailable(practice.getId());

        log.info("STT feedback saved successfully");
    }

    // 연습 ID로 STT 피드백 조회
    public SttDto getSttFeedbackByPracticeId(Long practiceId) {
        log.info("Fetching STT feedback for practice ID: {}", practiceId);

        SttData sttData = sttRepository.findByPracticeId(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("No STT feedback found for practice ID: " + practiceId));

        // 엔티티를 DTO로 변환
        return convertToDto(sttData);
    }

    // SttData 엔티티를 SttDto로 변환 (수정)
    private SttDto convertToDto(SttData sttData) {
        // 필러 통계 DTO 생성
        SttFillerStatisticsDto fillerStats = SttFillerStatisticsDto.builder()
                .eo(sttData.getFillerEo())
                .eum(sttData.getFillerEum())
                .geu(sttData.getFillerGeu())
                .totalFillerCount(sttData.getTotalFillerCount())
                .fillerRatio(sttData.getFillerRatio())
                .build();

        // 침묵 통계 DTO 생성
        SttSilenceStatisticsDto silenceStats = SttSilenceStatisticsDto.builder()
                .silenceRatio(sttData.getSilenceRatio())
                .speakingRatio(sttData.getSpeakingRatio())
                .totalPresentationTime(sttData.getTotalPresentationTime())
                .build();

        // 트랜스크립트 세그먼트 DTO 생성
        List<SttTranscriptSegmentDto> transcriptDtos = sttData.getTranscriptSegments().stream()
                .map(segment -> SttTranscriptSegmentDto.builder()
                        .start(segment.getStart())
                        .end(segment.getEnd())
                        .tag(segment.getTag())
                        .result(segment.getResult())
                        .build())
                .toList();

        // SttDto 생성 및 반환 (수정)
        return SttDto.builder()
                .userId(sttData.getUser().getId())
                .presentationId(sttData.getPresentation().getId())
                .practiceId(sttData.getPractice().getId())
                .fluencyScore(sttData.getFluencyScore()) // 수정: 루트 레벨 필드로 설정
                .statisticsFiller(List.of(fillerStats))
                .statisticsSilence(List.of(silenceStats))
                .transcript(transcriptDtos)
                .build();
    }
}