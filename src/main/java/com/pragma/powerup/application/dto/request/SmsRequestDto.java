package com.pragma.powerup.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsRequestDto {
    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{7,14}$",
            message = "must be in international format, for example +573005698325")
    private String phone;

    @NotBlank
    private String message;
}
