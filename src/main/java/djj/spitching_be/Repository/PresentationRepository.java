package djj.spitching_be.Repository;

import djj.spitching_be.Domain.Presentation;
import djj.spitching_be.Dto.PresentationListResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PresentationRepository extends JpaRepository <Presentation, Long> {
    List<Presentation> findAllByOrderByUpdatedAtDesc();

    List<Presentation> findByUserEmail(String email);

    // Service에서 사용하는 메서드 추가
    Optional<Presentation> findFirstByUserIdOrderByCreatedAtDesc(Long userId);


}
