package com.cybercore.companion.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InteractionResponse {
    private String interactionId;

    public InteractionResponse(String interactionId) {
        this.interactionId = interactionId;
    }
}
