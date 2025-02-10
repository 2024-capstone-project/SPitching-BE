package djj.spitching_be.Repository;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Dto.PresentationListResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PresentationRepository extends JpaRepository <Presentation, Long> {
    List<PresentationListResponseDto> findAllByOrderByModifiedAtDesc();

}
