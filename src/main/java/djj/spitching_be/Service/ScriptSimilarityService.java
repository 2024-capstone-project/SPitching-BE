package djj.spitching_be.Service;

import djj.spitching_be.Domain.Practice;
import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Domain.PresentationSlide;
import djj.spitching_be.Dto.SttDto;
import djj.spitching_be.Repository.PracticeRepository;
import djj.spitching_be.Repository.PresentationRepository;
import djj.spitching_be.Repository.PresentationSlideRepository;
import djj.spitching_be.Domain.TextSimilarityUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptSimilarityService {

    private final PracticeRepository practiceRepository;
    private final PresentationRepository presentationRepository;
    private final PresentationSlideRepository presentationSlideRepository;
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

            // transcript 체크
            if (sttDto.getTranscript() == null) {
                log.error("❌ [STEP 1-2] Transcript is null");
                return 0.0;
            }
            log.info("📝 [STEP 1-3] Transcript size: {}", sttDto.getTranscript().size());

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
            for (PresentationSlide slide : slides) {
                String slideScript = slide.getScript();
                log.info("📄 [STEP 3-2] Slide {} script: {}", slide.getSlideNumber(),
                        slideScript != null ? slideScript.substring(0, Math.min(50, slideScript.length())) + "..." : "null");
                if (slideScript != null && !slideScript.trim().isEmpty()) {
                    fullScript.append(slideScript).append(" ");
                }
            }

            String completeScript = fullScript.toString().trim();
            log.info("📖 [STEP 4] 완전한 대본 생성 - length: {}, preview: '{}'",
                    completeScript.length(),
                    completeScript.length() > 0 ? completeScript.substring(0, Math.min(100, completeScript.length())) + "..." : "EMPTY");

            if (completeScript.isEmpty()) {
                log.warn("❌ [STEP 4-1] Complete script is empty for presentation ID: {}", presentationId);
                return 0.0;
            }

            // STT 트랜스크립트에서 발화 내용 추출
            List<Map<String, Object>> transcriptList = new ArrayList<>();
            for (Object segment : sttDto.getTranscript()) {
                if (segment instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> segmentMap = (Map<String, Object>) segment;
                    transcriptList.add(segmentMap);

                    // 🔍 각 세그먼트의 내용을 로그로 확인
                    log.info("📊 [STEP 5-1] Segment: tag={}, result={}",
                            segmentMap.get("tag"), segmentMap.get("result"));
                }
            }
            log.info("📋 [STEP 5] 트랜스크립트 리스트 생성 완료 - 세그먼트 개수: {}", transcriptList.size());

            String transcribedText = textSimilarityUtil.extractSpeechFromTranscript(transcriptList);
            log.info("🎤 [STEP 6] 전사본 추출 완료 - length: {}, content: '{}'",
                    transcribedText.length(),
                    transcribedText.length() > 0 ? transcribedText.substring(0, Math.min(100, transcribedText.length())) + "..." : "EMPTY");

            if (transcribedText.isEmpty()) {
                log.warn("❌ [STEP 6-1] Transcribed text is empty for practice ID: {}", practiceId);
                return 0.0;
            }

            // 🔥 유사도 계산 직전
            log.info("🧮 [STEP 7] 유사도 계산 시작...");
            log.info("📖 [STEP 7-1] 대본 최종: '{}'", completeScript);
            log.info("🎤 [STEP 7-2] 전사본 최종: '{}'", transcribedText);

            double similarity = textSimilarityUtil.calculateCosineSimilarity(completeScript, transcribedText);
            log.info("✅ [STEP 8] 유사도 계산 완료 - similarity: {}", similarity);

            // 🔥 유사도 검증
            if (Double.isNaN(similarity)) {
                log.error("❌ [STEP 8-1] 유사도 값이 NaN: {}", similarity);
                similarity = 0.0;
            } else if (similarity < 0) {
                log.error("❌ [STEP 8-2] 유사도 값이 음수: {}", similarity);
                similarity = 0.0;
            } else if (similarity > 1.0) {
                log.warn("⚠️ [STEP 8-3] 유사도 값 > 1.0: {}", similarity);
                similarity = 1.0;
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