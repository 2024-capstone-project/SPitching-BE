package djj.spitching_be.Repository;

import djj.spitching_be.Domain.GestureData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GestureRepository extends JpaRepository<GestureData, Long> {
    // 사용자별 제스처 데이터 조회
    List<GestureData> findByUserId(Long userId);

    // 발표별 제스처 데이터 조회
    List<GestureData> findByPresentationId(Long presentationId);

    // 연습 ID로 조회
    List<GestureData> findByPracticeId(Long practiceId);

    // 사용자의 특정 발표에 대한 제스처 데이터 조회
    List<GestureData> findByUserIdAndPresentationId(Long userId, Long presentationId);


    // 사용자와 연습으로 조회
    List<GestureData> findByUserIdAndPracticeId(Long userId, Long practiceId);

    // 발표와 연습으로 조회
    List<GestureData> findByPresentationIdAndPracticeId(Long presentationId, Long practiceId);

    // 사용자, 발표, 연습으로 조회
    List<GestureData> findByUserIdAndPresentationIdAndPracticeId(Long userId, Long presentationId, Long practiceId);

    // 최신순으로 정렬하여 조회
    List<GestureData> findByPresentationIdOrderByCreatedAtDesc(Long presentationId);
}
