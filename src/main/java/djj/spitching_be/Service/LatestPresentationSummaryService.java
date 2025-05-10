package djj.spitching_be.Service;

import djj.spitching_be.Domain.*;
import djj.spitching_be.Dto.*;
import djj.spitching_be.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LatestPresentationSummaryService {

    private final PresentationRepository presentationRepository;
    private final PracticeRepository practiceRepository;
    private final PresentationSlideRepository presentationSlideRepository;
    private final TagRepository tagRepository;
    private final SttRepository sttRepository;
    private final GestureRepository gestureRepository;
    private final EyeRepository eyeRepository;

    /**
     * 특정 사용자의 가장 최근 발표 요약 정보를 조회합니다.
     */
    public LatestPresentationSummaryDto getLatestPresentationSummary(Long userId) {
        // 1. 가장 최근 발표 조회
        Presentation latestPresentation = presentationRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NoSuchElementException("No presentations found for user ID: " + userId));

        // 2. 해당 발표의 가장 최근 연습 조회
        Practice latestPractice = practiceRepository.findTopByPresentationIdOrderByPracticeDateDesc(latestPresentation.getId())
                .orElseThrow(() -> new NoSuchElementException("No practices found for presentation ID: " + latestPresentation.getId()));

        // 3. 해당 발표의 연습 개수 조회
        Integer practiceCount = practiceRepository.countByPresentationId(latestPresentation.getId());

        // 4. 해당 발표의 연습 점수 기록 조회 (최근 5개)
        List<Practice> recentPractices = practiceRepository.findTop5ByPresentationIdOrderByPracticeDateDesc(latestPresentation.getId());

        // 5. 가장 최근 연습의 각 점수 조회
        GraphDto graph = getScoreDetails(latestPractice.getId(), recentPractices);

        // 6. 슬라이드 및 태그 정보 조회
        List<TagDto> tags = getTagInformation(latestPresentation.getId());

        // 7. 첫 슬라이드 이미지 URL 조회
        String firstSlideImageUrl = presentationSlideRepository
                .findFirstByPresentationIdOrderBySlideNumber(latestPresentation.getId())
                .map(PresentationSlide::getImageUrl)
                .orElse(null);

        // 8. 응답 DTO 생성
        return LatestPresentationSummaryDto.builder()
                .presentationId(latestPresentation.getId())
                .practiceId(latestPractice.getId())
                .title(latestPresentation.getTitle())
                .description(latestPresentation.getDescription())
                .created(latestPresentation.getCreatedAt())
                .lastPractice(latestPractice.getPracticeDate())
                .practiceCount(practiceCount)
                .graph(graph)
                .tags(tags)
                .firstSlideImageUrl(firstSlideImageUrl)
                .build();
    }

    /**
     * 점수 상세 정보를 조회합니다.
     */
    private GraphDto getScoreDetails(Long practiceId, List<Practice> recentPractices) {
        // 가장 최근 연습의 점수 정보
        Practice latestPractice = recentPractices.get(0);
        Double currentScore = latestPractice.getTotalScore();

        // 이전 연습들의 점수 (최근 -> 옛날 순서)
        List<Double> previousScores = recentPractices.stream()
                .skip(1)  // 첫 번째(가장 최근) 제외
                .map(practice -> practice.getTotalScore())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 각 분야별 점수 조회
        Double eyeScore = getEyeScore(practiceId);
        Double gestureScore = getGestureScore(practiceId);
        Double sttScore = getSttScore(practiceId);
        Double cosineSimilarity = latestPractice.getScriptSimilarity();

        return GraphDto.builder()
                .currentScore(currentScore)
                .previousScores(previousScores)
                .eyeScore(eyeScore)
                .gestureScore(gestureScore)
                .sttScore(sttScore)
                .cosineSimilarity(cosineSimilarity)
                .build();
    }

    /**
     * 시선 점수 조회
     */
    private Double getEyeScore(Long practiceId) {
        return eyeRepository.findByPracticeId(practiceId)
                .map(eyeData -> eyeData.getEyecontactScore().doubleValue())
                .orElse(null);
    }

    /**
     * 제스처 점수 조회
     */
    private Double getGestureScore(Long practiceId) {
        return gestureRepository.findByPracticeId(practiceId)
                .map(gestureData -> gestureData.getGestureScore().doubleValue())
                .orElse(null);
    }

    /**
     * STT 점수 조회
     */
    private Double getSttScore(Long practiceId) {
        return sttRepository.findByPracticeId(practiceId)
                .map(SttData::getFluencyScore)
                .orElse(null);
    }

    /**
     * 태그 정보 조회
     */
    private List<TagDto> getTagInformation(Long presentationId) {
        // 발표의 모든 슬라이드 조회
        List<PresentationSlide> slides = presentationSlideRepository.findByPresentationIdOrderBySlideNumber(presentationId);

        // 슬라이드별 태그 정보 조회
        List<TagDto> tagDtos = new ArrayList<>();

        for (PresentationSlide slide : slides) {
            List<Tag> tags = tagRepository.findByPresentationSlideId(slide.getId());

            if (!tags.isEmpty()) {
                List<String> tagContents = tags.stream()
                        .map(Tag::getContent)
                        .collect(Collectors.toList());

                TagDto tagDto = TagDto.builder()
                        .page(slide.getSlideNumber())
                        .count(tags.size())
                        .notes(tagContents)
                        .build();

                tagDtos.add(tagDto);
            }
        }

        // 페이지 번호 기준으로 정렬
        tagDtos.sort(Comparator.comparing(TagDto::getPage));

        return tagDtos;
    }
}
