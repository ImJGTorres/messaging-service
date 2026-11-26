package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.application.dto.request.SmsRequestDto;
import com.pragma.powerup.application.handler.ISmsHandler;
import com.pragma.powerup.domain.exception.InvalidSmsException;
import com.pragma.powerup.domain.exception.SmsDeliveryException;
import com.pragma.powerup.infrastructure.exceptionhandler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SmsRestControllerTest {
    private static final String SMS_URL = "/api/v1/sms";
    private static final String PHONE_MESSAGE = "must be in international format, for example +573005698325";

    @Mock
    private ISmsHandler smsHandler;

    @InjectMocks
    private SmsRestController smsRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(smsRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String buildJson(String phone, String message) {
        return """
                {"phone": "%s", "message": "%s"}
                """.formatted(phone, message);
    }

    @Test
    void sendSmsWithValidDataReturns202WithoutBodyAndHandsTheDataToTheHandler() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("+573005698325", "Pedido #5 listo. Tu PIN es 482913.")))
                .andExpect(status().isAccepted())
                .andExpect(content().string(""));

        ArgumentCaptor<SmsRequestDto> requestCaptor = ArgumentCaptor.forClass(SmsRequestDto.class);
        verify(smsHandler).sendSms(requestCaptor.capture());
        assertEquals("+573005698325", requestCaptor.getValue().getPhone());
        assertEquals("Pedido #5 listo. Tu PIN es 482913.", requestCaptor.getValue().getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+12345678", "+573005698325", "+123456789012345"})
    void sendSmsWithAPhoneInInternationalFormatBetween8And15DigitsIsAccepted(String phone) throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson(phone, "Hola")))
                .andExpect(status().isAccepted());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3005698325", "573005698325", "+1234567", "+1234567890123456", "+0573005698325",
            "+57300abc8325", "+57 300 569 8325", "+"})
    void sendSmsWithAPhoneThatIsNotInternationalFormatReturns400ExplainingTheFormat(String phone) throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson(phone, "Hola")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").value(PHONE_MESSAGE));

        verifyNoInteractions(smsHandler);
    }

    @Test
    void sendSmsWithEmptyBodyReturns400ForThePhoneAndTheMessage() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.phone").exists())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void sendSmsWithABlankMessageReturns400() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("+573005698325", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void sendSmsWithBrokenJsonReturns400WithAShortMessageAndNoInternalDetails() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or invalid JSON request"))
                .andExpect(jsonPath("$.trace").doesNotExist());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void sendSmsWhenTheDomainRejectsTheMessageReturns400WithTheDomainMessage() throws Exception {
        doThrow(new InvalidSmsException("The message must have between 1 and 160 characters"))
                .when(smsHandler).sendSms(any(SmsRequestDto.class));

        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("+573005698325", "a".repeat(161))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The message must have between 1 and 160 characters"));
    }

    @Test
    void sendSmsWhenTheProviderIsNotAvailableReturns503WithAGenericMessage() throws Exception {
        doThrow(new SmsDeliveryException("The SMS provider is not available"))
                .when(smsHandler).sendSms(any(SmsRequestDto.class));

        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildJson("+573005698325", "Hola")))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("The SMS provider is not available"));
    }
}
