package djj.spitching_be.Repository;

import djj.spitching_be.Domain.EyeData;
import djj.spitching_be.Domain.GestureData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EyeRepository extends JpaRepository<EyeData, Long> {
    // 사용자별 시선 추적 데이터 조회
    List<EyeData> findByUserId(Long userId);

    // 발표별 시선 추적 데이터 조회
    List<EyeData> findByPresentationId(Long presentationId);

    // 연습 ID로 단일 시선 추적 데이터 조회(1:1 관계)
    Optional<EyeData> findByPracticeId(Long practiceId);

    // 사용자의 특정 발표에 대한 제스처 데이터 조회
    List<EyeData> findByUserIdAndPresentationId(Long userId, Long presentationId);
}
