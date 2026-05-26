package com.blood.dto.ReturnLog;

import lombok.Data;
import java.util.List;

@Data
public class BulkInspectRequest {
    private Integer returnOrderId;

    private List<InspectItemRequest> items;
}
