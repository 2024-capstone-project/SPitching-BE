package djj.spitching_be.Repository;

import djj.spitching_be.Domain.PresentationSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresentationSlideRepository extends JpaRepository<PresentationSlide, Long> {
    List<PresentationSlide> findByPresentationId(Long presentationId);
    Optional<PresentationSlide> findByPresentationIdAndSlideNumber(Long presentationId, Integer slideNumber);

    // 특정 발표의 모든 슬라이드 조회 (슬라이드 번호 순으로 정렬)
    List<PresentationSlide> findByPresentationIdOrderBySlideNumber(Long presentationId);

    // 특정 발표의 첫 번째 슬라이드 조회
    Optional<PresentationSlide> findFirstByPresentationIdOrderBySlideNumber(Long presentationId);


}
