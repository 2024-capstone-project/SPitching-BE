package djj.spitching_be.Repository;

import djj.spitching_be.Domain.Practice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PracticeRepository extends JpaRepository <Practice, Long> {
    // 특정 발표의 최근 5개 연습 조회
    List<Practice> findTop5ByPresentationIdOrderByPracticeDateDesc(Long presentationId);

    // 특정 발표의 가장 최근 연습 조회
    Optional<Practice> findTopByPresentationIdOrderByPracticeDateDesc(Long presentationId);

    // 특정 발표의 연습 개수 조회
    Integer countByPresentationId(Long presentationId);
}
