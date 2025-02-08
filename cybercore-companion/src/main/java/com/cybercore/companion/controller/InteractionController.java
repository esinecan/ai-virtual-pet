package com.cybercore.companion.controller;

import com.cybercore.companion.dto.InteractionStatusResponse;
import com.cybercore.companion.service.InteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @GetMapping("/{interactionId}/status")
    public ResponseEntity<InteractionStatusResponse> getInteractionStatus(@PathVariable String interactionId) {
        return ResponseEntity.ok(interactionService.getInteractionStatus(interactionId));
    }
}
