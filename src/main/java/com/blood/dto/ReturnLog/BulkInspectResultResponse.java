package com.blood.dto.ReturnLog;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BulkInspectResultResponse {
    private String summary;
    private int successCount;
    private int pendingHuyCount;
    private int pendingKiemDinhCount;
    private boolean allOrderProcessed;
    private List<String> errors;
}
