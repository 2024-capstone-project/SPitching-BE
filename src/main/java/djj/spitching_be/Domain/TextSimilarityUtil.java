package djj.spitching_be.Domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TextSimilarityUtil {

    // 확장된 불용어 목록
    private static final Set<String> KOREAN_STOPWORDS = new HashSet<>(Arrays.asList(
            // 조사
            "이", "그", "저", "것", "수", "를", "에", "은", "는", "이", "가", "을", "를", "에서", "의", "으로",
            "로", "에게", "뿐", "다", "도", "만", "까지", "에도", "조차", "마저", "라도", "든지",
            "로서", "로써", "서", "써", "커녕", "치고", "하고", "이라고", "라고", "이라는", "라는",
            // 접속사, 감탄사
            "그리고", "그런데", "하지만", "그러나", "또한", "또", "그래서", "따라서", "즉", "아", "어", "음", "어어",
            // 대명사
            "나", "너", "우리", "그들", "이것", "그것", "저것", "여기", "거기", "저기",
            // 시간 관련
            "때", "시간", "동안", "후", "전", "지금", "오늘", "내일", "어제",
            // STT 특화 불용어 (추임새, 간투사)
            "어음", "음음", "아아", "네네", "예예", "잠깐", "잠시"
    ));

    // STT에서 자주 발생하는 동의어/유사어 매핑
    private static final Map<String, String> SYNONYM_MAP = new HashMap<>();

    static {
        // 숫자 표현 통일
        SYNONYM_MAP.put("하나", "1");
        SYNONYM_MAP.put("둘", "2");
        SYNONYM_MAP.put("셋", "3");
        SYNONYM_MAP.put("넷", "4");
        SYNONYM_MAP.put("다섯", "5");

        // 자주 혼동되는 표현들
        SYNONYM_MAP.put("됩니다", "된다");
        SYNONYM_MAP.put("입니다", "이다");
        SYNONYM_MAP.put("습니다", "한다");
        SYNONYM_MAP.put("해요", "한다");
        SYNONYM_MAP.put("이에요", "이다");
        SYNONYM_MAP.put("예요", "이다");
    }

    /**
     * 향상된 텍스트 정제 메소드
     */
    public String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String cleaned = text;

        // STT 특화 정제
        // 괄호 안의 내용 제거 (시간, 추임새 등)
        cleaned = cleaned.replaceAll("\\([^)]*\\)", "");

        // 반복되는 추임새 제거
        cleaned = cleaned.replaceAll("\\b(어|음|아|네|예)\\s*\\1+\\b", "$1");

        // 특수 문자 제거 (한글, 영문, 숫자, 공백만 유지)
        cleaned = cleaned.replaceAll("[^\\p{L}\\p{N}\\s]", " ");

        // 연속된 공백을 하나로
        cleaned = cleaned.replaceAll("\\s+", " ");

        return cleaned.trim().toLowerCase();
    }

    /**
     * 향상된 토큰화 (n-gram 포함)
     */
    public List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String[] words = text.split("\\s+");
        List<String> tokens = new ArrayList<>();

        // 1-gram (단어 단위)
        for (String word : words) {
            if (!word.isEmpty() && !KOREAN_STOPWORDS.contains(word)) {
                // 동의어 매핑 적용
                String normalizedWord = SYNONYM_MAP.getOrDefault(word, word);
                tokens.add(normalizedWord);
            }
        }

        // 2-gram 추가 (의미 있는 구문 보존)
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];

            if (!KOREAN_STOPWORDS.contains(word1) && !KOREAN_STOPWORDS.contains(word2)) {
                String bigram = word1 + "_" + word2;
                tokens.add(bigram);
            }
        }

        return tokens;
    }

    /**
     * TF-IDF 가중치를 고려한 단어 빈도 계산
     */
    public Map<String, Double> getTfIdfWeights(List<String> terms) {
        Map<String, Integer> termFreq = new HashMap<>();
        Map<String, Double> tfIdf = new HashMap<>();

        // Term Frequency 계산
        for (String term : terms) {
            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);
        }

        int totalTerms = terms.size();

        // 간단한 TF-IDF 계산 (IDF는 단순화)
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            int frequency = entry.getValue();

            // TF = (단어 빈도) / (전체 단어 수)
            double tf = (double) frequency / totalTerms;

            // 바이그램에 더 높은 가중치 부여
            double weight = term.contains("_") ? tf * 1.5 : tf;

            tfIdf.put(term, weight);
        }

        return tfIdf;
    }

    /**
     * 향상된 코사인 유사도 계산
     */
    public double calculateCosineSimilarity(String text1, String text2) {
        // 텍스트 정제
        String cleanedText1 = cleanText(text1);
        String cleanedText2 = cleanText(text2);

        log.debug("Cleaned text1: {}", cleanedText1);
        log.debug("Cleaned text2: {}", cleanedText2);

        // 텍스트 토큰화
        List<String> tokens1 = tokenize(cleanedText1);
        List<String> tokens2 = tokenize(cleanedText2);

        log.debug("Tokens1: {}", tokens1);
        log.debug("Tokens2: {}", tokens2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        // 모든 고유 단어 수집
        Set<String> uniqueTerms = new HashSet<>();
        uniqueTerms.addAll(tokens1);
        uniqueTerms.addAll(tokens2);

        // TF-IDF 가중치 계산
        Map<String, Double> weights1 = getTfIdfWeights(tokens1);
        Map<String, Double> weights2 = getTfIdfWeights(tokens2);

        // 코사인 유사도 계산
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String term : uniqueTerms) {
            double weight1 = weights1.getOrDefault(term, 0.0);
            double weight2 = weights2.getOrDefault(term, 0.0);

            dotProduct += weight1 * weight2;
            norm1 += Math.pow(weight1, 2);
            norm2 += Math.pow(weight2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        log.debug("Calculated similarity: {}", similarity);

        return similarity;
    }

    /**
     * Jaccard 유사도 계산 (보조 지표)
     */
    public double calculateJaccardSimilarity(String text1, String text2) {
        List<String> tokens1 = tokenize(cleanText(text1));
        List<String> tokens2 = tokenize(cleanText(text2));

        Set<String> set1 = new HashSet<>(tokens1);
        Set<String> set2 = new HashSet<>(tokens2);

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size() / union.size();
    }

    /**
     * 종합 유사도 계산 (코사인과 Jaccard의 가중평균)
     */
    public double calculateCombinedSimilarity(String text1, String text2) {
        double cosineSim = calculateCosineSimilarity(text1, text2);
        double jaccardSim = calculateJaccardSimilarity(text1, text2);

        // 코사인 유사도에 더 높은 가중치 (0.7:0.3)
        return 0.7 * cosineSim + 0.3 * jaccardSim;
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

            if (tag != null && tag.equals("1000") && result != null) {
                speech.append(result).append(" ");
            }
        }

        return speech.toString().trim();
    }
}