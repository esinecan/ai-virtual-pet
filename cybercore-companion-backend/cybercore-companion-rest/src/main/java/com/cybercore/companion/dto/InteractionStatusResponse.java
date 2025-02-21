package com.cybercore.companion.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InteractionStatusResponse {
    private String status;
    private String response;

    public InteractionStatusResponse(String status, String response) {
        this.status = status;
        this.response = response;
    }
}
