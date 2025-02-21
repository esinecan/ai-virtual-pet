package com.cybercore.companion.controller;

import com.cybercore.companion.dto.CorelingStateDto;
import com.cybercore.companion.dto.InteractionRequest;
import com.cybercore.companion.service.CorelingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CorelingController {

    private final CorelingService corelingService;

    @GetMapping("/status")
    public String getStatus() {
        return "REST API is running";
    }

    @GetMapping("/coreling/{userAccountId}/state")
    public CorelingStateDto getCorelingState(@PathVariable Long userAccountId) {
        return corelingService.getCorelingState(userAccountId);
    }

    @PostMapping("/coreling/{userAccountId}/interact")
    public String processInteraction(
            @PathVariable Long userAccountId,
            @Valid @RequestBody InteractionRequest request) {
        return corelingService.processInteraction(userAccountId, request.getMessage());
    }
}
