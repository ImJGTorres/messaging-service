package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.SmsRequestDto;
import com.pragma.powerup.application.handler.ISmsHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SmsRestController {
    private final ISmsHandler smsHandler;

    @Operation(summary = "Send an SMS (accepted by the provider; delivery is not guaranteed)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "SMS accepted", content = @Content),
            @ApiResponse(responseCode = "400",
                    description = "Phone not in international format, blank message or message over 160 characters",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Only an employee can send SMS", content = @Content),
            @ApiResponse(responseCode = "503", description = "The SMS provider is not available", content = @Content)
    })
    @PostMapping(value = "/sms", consumes = "application/json")
    public ResponseEntity<Void> sendSms(@Valid @RequestBody SmsRequestDto smsRequestDto) {
        smsHandler.sendSms(smsRequestDto);
        return ResponseEntity.accepted().build();
    }
}
