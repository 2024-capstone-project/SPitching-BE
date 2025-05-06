package djj.spitching_be.Repository;

import djj.spitching_be.Domain.SttTranscriptSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SttTranscriptRepository extends JpaRepository<SttTranscriptSegment, Long> {
    List<SttTranscriptSegment> findBySttDataId(Long sttDataId);
}
