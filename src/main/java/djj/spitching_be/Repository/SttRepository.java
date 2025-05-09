package djj.spitching_be.Repository;

import djj.spitching_be.Domain.SttData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SttRepository extends JpaRepository<SttData, Long> {
    // 사용자별 STT 데이터 조회
    List<SttData> findByUserId(Long userId);

    // 발표별 STT 데이터 조회
    List<SttData> findByPresentationId(Long presentationId);

    // 연습 ID로 단일 STT 데이터 조회(1:1 관계)
    Optional<SttData> findByPracticeId(Long practiceId);

    // 사용자의 특정 발표에 대한 STT 데이터 조회
    List<SttData> findByUserIdAndPresentationId(Long userId, Long presentationId);

}
