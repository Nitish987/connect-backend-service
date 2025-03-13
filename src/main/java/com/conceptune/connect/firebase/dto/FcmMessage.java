package com.conceptune.connect.firebase.dto;

import com.conceptune.connect.constants.FcmQuery;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class FcmMessage {
    private FcmQuery query;
    private String message;
    private Map<String, Object> data;
}
