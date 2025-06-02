package djj.spitching_be.Service;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Domain.SttData;
import djj.spitching_be.Domain.SttTranscriptSegment;
import djj.spitching_be.Dto.SttDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Repository.SttRepository;
import djj.spitching_be.Repository.SttTranscriptRepository;
import djj.spitching_be.Domain.TextSimilarityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptSimilarityService {

    private final PracticeRepository practiceRepository;
    private final PresentationRepository presentationRepository;
    private final PresentationSlideRepository presentationSlideRepository;
    private final SttRepository sttRepository;
    private final SttTranscriptRepository sttTranscriptRepository;
    private final TextSimilarityUtil textSimilarityUtil;

    /**
     * 전사본과 전체 대본의 유사도를 계산하고 저장
     */
    @Transactional
    public double calculateAndSaveScriptSimilarity(SttDto sttDto) {
        log.info("🚀 [STEP 1] 유사도 계산 메서드 시작 - practiceId: {}, presentationId: {}",
                sttDto.getPracticeId(), sttDto.getPresentationId());

        try {
            Long practiceId = sttDto.getPracticeId();
            Long presentationId = sttDto.getPresentationId();

            // 기본 null 체크
            if (sttDto == null || practiceId == null || presentationId == null) {
                log.error("❌ [STEP 1-1] Invalid SttDto: practiceId={}, presentationId={}", practiceId, presentationId);
                return 0.0;
            }

            log.info("✅ [STEP 1-2] 기본 검증 완료 - practiceId: {}, presentationId: {}", practiceId, presentationId);

            // 연습 정보 조회
            Practice practice = practiceRepository.findById(practiceId)
                    .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));
            log.info("✅ [STEP 2] Practice 조회 완료 - ID: {}", practiceId);

            // 발표의 모든 슬라이드 조회하여 전체 대본 생성
            List<PresentationSlide> slides = presentationSlideRepository.findByPresentationIdOrderBySlideNumber(presentationId);
            log.info("📑 [STEP 3] 슬라이드 조회 완료 - 슬라이드 개수: {}", slides.size());

            if (slides.isEmpty()) {
                log.warn("❌ [STEP 3-1] No slides found for presentation ID: {}", presentationId);
                return 0.0;
            }

            // 모든 슬라이드의 대본을 하나로 합쳐서 전체 대본 생성
            StringBuilder fullScript = new StringBuilder();
            int scriptSlideCount = 0;
            for (PresentationSlide slide : slides) {
                String slideScript = slide.getScript();
                if (slideScript != null && !slideScript.trim().isEmpty()) {
                    fullScript.append(slideScript).append(" ");
                    scriptSlideCount++;
                    log.info("📄 [STEP 3-2] Slide {} script: {}", slide.getSlideNumber(),
                            slideScript.substring(0, Math.min(50, slideScript.length())) + "...");
                }
            }

            String completeScript = fullScript.toString().trim();
            log.info("📖 [STEP 4] 완전한 대본 생성 - 스크립트가 있는 슬라이드: {}/{}, 총 length: {}, preview: '{}'",
                    scriptSlideCount, slides.size(), completeScript.length(),
                    completeScript.length() > 0 ? completeScript.substring(0, Math.min(100, completeScript.length())) + "..." : "EMPTY");

            if (completeScript.isEmpty()) {
                log.warn("❌ [STEP 4-1] Complete script is empty for presentation ID: {}", presentationId);
                return 0.0;
            }

            // ✅ 새로운 방식: DB에서 저장된 전사본 조회
            log.info("🔍 [STEP 5] DB에서 전사본 조회 시작 - practiceId: {}", practiceId);

            // 먼저 practice에 연결된 SttData 조회
            Optional<SttData> sttDataOpt = sttRepository.findByPracticeId(practiceId);
            if (sttDataOpt.isEmpty()) {
                log.warn("❌ [STEP 5-1] STT 데이터를 찾을 수 없음 - practiceId: {}", practiceId);
                return 0.0;
            }

            SttData sttData = sttDataOpt.get();
            log.info("✅ [STEP 5-2] STT 데이터 조회 완료 - sttDataId: {}", sttData.getId());

            // SttData ID로 전사본 세그먼트들 조회
            List<SttTranscriptSegment> transcriptSegments = sttTranscriptRepository.findBySttDataId(sttData.getId());
            log.info("📊 [STEP 5-3] DB에서 조회된 전체 STT 세그먼트 개수: {}", transcriptSegments.size());

            // tag별 분포 확인
            Map<String, Integer> tagCount = new HashMap<>();
            for (SttTranscriptSegment segment : transcriptSegments) {
                String tag = segment.getTag();
                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                log.info("📊 [STEP 5-4] STT Segment - tag: {}, result: {}",
                        tag, segment.getResult() != null ?
                                segment.getResult().substring(0, Math.min(30, segment.getResult().length())) + "..." : "null");
            }
            log.info("📊 [STEP 5-5] Tag 분포: {}", tagCount);

            // tag가 "1000"인 발화 내용만 추출
            StringBuilder transcribedTextBuilder = new StringBuilder();
            int speechCount = 0;
            for (SttTranscriptSegment segment : transcriptSegments) {
                if ("1000".equals(segment.getTag()) && segment.getResult() != null && !segment.getResult().trim().isEmpty()) {
                    transcribedTextBuilder.append(segment.getResult().trim()).append(" ");
                    speechCount++;
                }
            }

            String transcribedText = transcribedTextBuilder.toString().trim();
            log.info("🎤 [STEP 6] DB에서 추출한 전사본 - 발화 세그먼트 수: {}, 총 length: {}, content: '{}'",
                    speechCount, transcribedText.length(),
                    transcribedText.length() > 0 ? transcribedText.substring(0, Math.min(100, transcribedText.length())) + "..." : "EMPTY");

            if (transcribedText.isEmpty()) {
                log.warn("❌ [STEP 6-1] DB에서 조회한 전사본이 비어있음 - practiceId: {}", practiceId);
                log.warn("❌ [STEP 6-2] 전체 STT 세그먼트는 {}개 있지만 tag='1000'인 발화 데이터가 없음", transcriptSegments.size());
                return 0.0;
            }

            // 🔥 유사도 계산 직전
            log.info("🧮 [STEP 7] 유사도 계산 시작...");
            log.info("📖 [STEP 7-1] 대본 최종 (length: {}): '{}'", completeScript.length(),
                    completeScript.length() > 200 ? completeScript.substring(0, 200) + "..." : completeScript);
            log.info("🎤 [STEP 7-2] 전사본 최종 (length: {}): '{}'", transcribedText.length(),
                    transcribedText.length() > 200 ? transcribedText.substring(0, 200) + "..." : transcribedText);

            double similarity = textSimilarityUtil.calculateCosineSimilarity(completeScript, transcribedText);
            log.info("✅ [STEP 8] 유사도 계산 완료 - raw similarity: {}", similarity);

            // 🔥 유사도 검증 (백분율 기준으로 수정)
            if (Double.isNaN(similarity)) {
                log.error("❌ [STEP 8-1] 유사도 값이 NaN: {}", similarity);
                similarity = 0.0;
            } else if (similarity < 0) {
                log.error("❌ [STEP 8-2] 유사도 값이 음수: {}", similarity);
                similarity = 0.0;
            } else if (similarity > 100.0) {
                log.warn("⚠️ [STEP 8-3] 유사도 값 > 100.0: {}", similarity);
                similarity = 100.0;
            }
            log.info("✅ [STEP 8-4] 유사도 검증 완료 - final similarity: {}", similarity);

            // 🔥 DB 저장 직전
            log.info("💾 [STEP 9] DB 저장 시작 - 저장할 유사도: {}", similarity);
            Double beforeSave = practice.getScriptSimilarity();
            log.info("📊 [STEP 9-1] 저장 전 기존 값: {}", beforeSave);

            practice.setScriptSimilarity(similarity);

            // 🔥 DB 저장 후 확인
            Practice savedPractice = practiceRepository.save(practice);
            log.info("✅ [STEP 10] DB 저장 완료 - 저장된 유사도: {}", savedPractice.getScriptSimilarity());

            // 🔥 최종 확인 - DB에서 다시 조회
            Practice reloadedPractice = practiceRepository.findById(practiceId).orElse(null);
            if (reloadedPractice != null) {
                log.info("🔍 [STEP 11] DB 재조회 결과 - 유사도: {}", reloadedPractice.getScriptSimilarity());
            } else {
                log.error("❌ [STEP 11] DB 재조회 실패");
            }

            log.info("🎉 [STEP 12] Script similarity calculated and saved for practice ID: {}, similarity: {}",
                    practiceId, similarity);

            return similarity;

        } catch (Exception e) {
            log.error("💥 [ERROR] 유사도 계산 중 예외 발생", e);
            return 0.0;
        }
    }

    /**
     * 특정 연습의 스크립트 유사도 조회
     */
    public double getScriptSimilarity(Long practiceId) {
        log.info("🔍 [GET] 유사도 조회 시작 - practiceId: {}", practiceId);

        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new EntityNotFoundException("Practice not found with ID: " + practiceId));

        Double similarity = practice.getScriptSimilarity();
        log.info("📊 [GET] 조회된 유사도: {}", similarity);

        return similarity != null ? similarity : 0.0;
    }
}