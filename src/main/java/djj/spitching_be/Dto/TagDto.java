package djj.spitching_be.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TagDto {
    private Integer page;
    private Integer count;
    private List<String> notes;
}