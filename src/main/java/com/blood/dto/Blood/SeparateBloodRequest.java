package com.blood.dto.Blood;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeparateBloodRequest {
    private Integer plasmaVolume;

    private Integer redCellVolume;

    private Integer plateletsVolume;

    private String overrideReason;
}
