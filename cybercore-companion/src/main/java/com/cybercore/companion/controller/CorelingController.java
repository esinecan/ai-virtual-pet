package com.cybercore.companion.controller;

import com.cybercore.companion.dto.CorelingStateDto;
import com.cybercore.companion.dto.InteractionRequest;
import com.cybercore.companion.dto.InteractionResponse;
import com.cybercore.companion.service.CorelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coreling")
@RequiredArgsConstructor
public class CorelingController {

    private final CorelingService corelingService;

    @GetMapping("/{userAccountId}")
    public ResponseEntity<CorelingStateDto> getCorelingState(@PathVariable Long userAccountId) {
        return ResponseEntity.ok(corelingService.getCorelingState(userAccountId));
    }

    @PostMapping("/{userAccountId}/interact")
    public ResponseEntity<InteractionResponse> initiateInteraction(
            @PathVariable Long userAccountId,
            @RequestBody InteractionRequest request
    ) {
        String interactionId = corelingService.processInteraction(userAccountId, request.getMessage());
        return ResponseEntity.accepted().body(new InteractionResponse(interactionId));
    }
}
