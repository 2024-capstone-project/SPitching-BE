package djj.spitching_be.Domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // Entity가 자동으로 컬럼으로 인식한다.
@EntityListeners(AuditingEntityListener.class) // 생성/변경 시간을 자동으로 업데이트한다.
public class Timestamped {
    @CreatedDate
    @Column(updatable = false) // 생성 시에만 설정, 수정 불가
    private LocalDateTime created_at;

    @LastModifiedDate
    private LocalDateTime updated_at;

    @Column(nullable = true) // 삭제되지 않은 경우 null
    private LocalDateTime deleted_at;

    // 삭제 시간을 설정하는 메서드 추가
    public void setDeletedAt() {
        this.deleted_at = LocalDateTime.now(); // 현재 시간으로 설정
    }
}
