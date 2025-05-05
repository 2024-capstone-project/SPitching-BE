package djj.spitching_be.Repository;

import djj.spitching_be.Domain.PresentationSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresentationSlideRepository extends JpaRepository<PresentationSlide, Long> {
    List<PresentationSlide> findByPresentationId(Long presentationId);
    Optional<PresentationSlide> findByPresentationIdAndSlideNumber(Long presentationId, Integer slideNumber);
}
