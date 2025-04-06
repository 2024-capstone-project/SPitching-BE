package djj.spitching_be.Repository;

import djj.spitching_be.Domain.GestureData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestureRepository extends JpaRepository<GestureData, Long> {

}
