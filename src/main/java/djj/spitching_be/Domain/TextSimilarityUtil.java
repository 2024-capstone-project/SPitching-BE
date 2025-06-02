package djj.spitching_be.Domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TextSimilarityUtil {

    // 불용어(stopwords) 목록 - 한국어 기준
    // 한국어 문장에서 매우 자주 등장하지만, 문장의 핵심 의미를 담고 있지 않은 단어들은 유사도를 지나치게 높일 수 있으므로 제거!
    private static final Set<String> KOREAN_STOPWORDS = new HashSet<>(Arrays.asList(
            "이", "그", "저", "것", "수", "를", "에", "은", "는", "이", "가", "을", "를", "에서", "의", "으로",
            "로", "에게", "뿐", "다", "도", "만", "까지", "에도", "조차", "마저", "라도", "든지",
            "로서", "로써", "서", "써", "커녕", "치고", "하고", "이라고", "라고", "이라는", "라는",
            // 추임새 추가
            "어", "음", "아", "네", "예", "잠깐", "잠시", "어음", "음음", "아아", "네네", "예예"
    ));

    /**
     * 텍스트에서 불필요한 내용을 제거하고 정제하는 메소드
     */
    public String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        // 특수 태그 제거 (예: '(1초)..', '어(추임새)' 등)
        String cleaned = text.replaceAll("\\([^)]*\\)\\.\\.", "");
        cleaned = cleaned.replaceAll("\\([^)]*추임새\\)", "");

        // 특수 문자 및 숫자 제거
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{Z}]", " ");

        // 여러 공백을 하나로 치환
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.trim().toLowerCase();
    }

    /**
     * 문자열을 단어로 분리하고 불용어 제거
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 문자열을 단어로 분리
        String[] words = text.split("\\s+");

        // 불용어 제거 및 빈 문자열 제거
        return Arrays.stream(words)
                .filter(word -> !word.isEmpty())
                .filter(word -> !KOREAN_STOPWORDS.contains(word))
                .collect(Collectors.toList());
    }

    /**
     * 단어 빈도 맵 생성
     */
    public Map<String, Integer> getTermFrequency(List<String> terms) {
        Map<String, Integer> termFrequency = new HashMap<>();

        for (String term : terms) {
            termFrequency.put(term, termFrequency.getOrDefault(term, 0) + 1);
        }

        return termFrequency;
    }

    /**
     * 두 텍스트 간의 코사인 유사도 계산 (백분율 반환)
     */
    public double calculateCosineSimilarity(String text1, String text2) {
        // 텍스트 정제
        String cleanedText1 = cleanText(text1);
        String cleanedText2 = cleanText(text2);

        // 텍스트 토큰화
        List<String> tokens1 = tokenize(cleanedText1);
        List<String> tokens2 = tokenize(cleanedText2);

        // 두 텍스트의 단어 집합
        Set<String> uniqueTerms = new HashSet<>();
        uniqueTerms.addAll(tokens1);
        uniqueTerms.addAll(tokens2);

        // 단어 빈도 맵 생성
        Map<String, Integer> termFreq1 = getTermFrequency(tokens1);
        Map<String, Integer> termFreq2 = getTermFrequency(tokens2);

        // 코사인 유사도 계산
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : uniqueTerms) {
            int freq1 = termFreq1.getOrDefault(term, 0);
            int freq2 = termFreq2.getOrDefault(term, 0);

            dotProduct += freq1 * freq2;
            norm1 += Math.pow(freq1, 2);
            norm2 += Math.pow(freq2, 2);
        }

        // 0으로 나누기 방지
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));

        // 백분율로 변환하여 반환 (0~100)
        return similarity * 100;
    }

    /**
     * STT 트랜스크립트에서 발화 내용만 추출
     */
    public String extractSpeechFromTranscript(List<Map<String, Object>> transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return "";
        }

        StringBuilder speech = new StringBuilder();

        for (Map<String, Object> segment : transcript) {
            String tag = (String) segment.get("tag");
            String result = (String) segment.get("result");

            // 발화 내용만 추출 (태그가 1000인 경우)
            if (tag != null && tag.equals("1000") && result != null) {
                speech.append(result).append(" ");
            }
        }

        return speech.toString().trim();
    }
}