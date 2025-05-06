package djj.spitching_be.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SttTranscriptSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long start;
    private Long end;
    private String tag;
    private String result;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stt_data_id")
    private SttData sttData;
}
