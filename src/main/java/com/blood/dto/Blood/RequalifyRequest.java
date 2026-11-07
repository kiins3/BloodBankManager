package com.blood.dto.Blood;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Result of re-checking bags a hospital returned (status CHO_KIEM_DINH). */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequalifyRequest {
    private List<Integer> bloodBagIds;

    /** true: the bag is still usable and goes back to storage; false: it is sent for disposal. */
    private Boolean passed;

    private String note;
}
