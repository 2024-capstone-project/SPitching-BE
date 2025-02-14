package djj.spitching_be.Domain;

import djj.spitching_be.Dto.TagRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor // Tag tag = new Tag(); 이렇게 빈 객체 생성이 가능해짐
@AllArgsConstructor // Tag tag = new Tag(1L, "Tag Content", LocalDateTime.now(), presentationSlide); 모든 필드 한번에 초기화 가능
@Table(name= "tags")
@EntityListeners(AuditingEntityListener.class) // 자동으로 시간 관리 가능
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @CreatedDate // 생성 시간 자동 설정 (Spring Data JPA)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // slide_id가 null이 되지 않게
    @JoinColumn(name="slide_id", nullable = false) // db에서도 null 되지 않게
    private PresentationSlide presentationSlide;

    // TagRequestDto를 받을 수 있게 만드는 생성자 추가
    public Tag(TagRequestDto tagRequestDto, PresentationSlide presentationSlide){
        this.content = tagRequestDto.getContent();
        this.presentationSlide = presentationSlide; // presentationSlide는 전달된 슬라이드 정보로 설정
    }

}
