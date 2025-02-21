package com.cybercore.companion.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
public class InteractionRequest {
    @NotEmpty(message = "Message cannot be empty")
    private String message;

    public InteractionRequest(String message) {
        this.message = message;
    }
}
