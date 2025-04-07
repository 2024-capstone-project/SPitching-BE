package djj.spitching_be.Repository;

import djj.spitching_be.Domain.GestureData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GestureRepository extends JpaRepository<GestureData, Long> {
    // 사용자별 제스처 데이터 조회
    List<GestureData> findByUserId(Long userId);

    // 발표별 제스처 데이터 조회
    List<GestureData> findByPresentationId(Long presentationId);

    // 사용자의 특정 발표에 대한 제스처 데이터 조회
    List<GestureData> findByUserIdAndPresentationId(Long userId, Long presentationId);

    // 최신순으로 정렬하여 조회
    List<GestureData> findByPresentationIdOrderByCreatedAtDesc(Long presentationId);
}
