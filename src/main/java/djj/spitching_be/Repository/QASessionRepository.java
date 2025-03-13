package djj.spitching_be.Repository;


import djj.spitching_be.Domain.QASession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QASessionRepository extends JpaRepository<QASession, Long> {
    Optional<QASession> findTopByPresentationIdOrderByCreatedAtDesc(Long presentationId);
}