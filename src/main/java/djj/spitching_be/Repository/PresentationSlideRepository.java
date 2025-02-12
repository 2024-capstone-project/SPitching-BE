package djj.spitching_be.Repository;

import djj.spitching_be.Domain.PresentationSlide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresentationSlideRepository extends JpaRepository<PresentationSlide, Long> {
    List<PresentationSlide> findByPresentationId(Long presentationId);
}
