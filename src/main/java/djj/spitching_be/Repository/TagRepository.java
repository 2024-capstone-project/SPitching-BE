package djj.spitching_be.Repository;

import djj.spitching_be.Domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag,Long> {
    // 특정 슬라이드에 해당하는 모든 태그를 조회
    List<Tag> findByPresentationSlideId(Long slide_id);

    // 특정 슬라이드 ID에 해당하는 태그 수를 카운트하는 메서드
    int countTagByPresentationSlideId(Long slide_id);

    // 특정 슬라이드와 연관된 모든 태그를 삭제하는 메서드 (슬라이드를 삭제했을 때 태그도 삭제되어야 하므로)
    @Transactional
    @Modifying
    @Query("DELETE FROM Tag t  WHERE t.presentationSlide.id = :slide_id")
    void deleteTagByPresentationSlideId(Long slide_id);
}
