package com.pragma.powerup.infrastructure.security;

import com.pragma.powerup.application.dto.request.SmsRequestDto;
import com.pragma.powerup.application.handler.ISmsHandler;
import com.pragma.powerup.infrastructure.input.rest.SmsRestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SmsRestController.class)
@Import({SecurityConfiguration.class, SecurityConfigurationTest.JwtTestConfiguration.class})
class SecurityConfigurationTest {
    private static final String SMS_URL = "/api/v1/sms";
    private static final String AUTHORIZATION = "Authorization";
    private static final String VALID_SMS_JSON = "{\"phone\": \"+573005698325\", \"message\": \"Hola\"}";

    @TestConfiguration
    static class JwtTestConfiguration {
        @Bean
        JwtTokenParser jwtTokenParser() {
            return new JwtTokenParser(JwtTestTokens.SECRET);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ISmsHandler smsHandler;

    private String bearerTokenFor(String role) {
        return "Bearer " + JwtTestTokens.tokenFor(JwtTestTokens.SECRET, 24L, role);
    }

    @Test
    void postSmsWithoutTokenReturns401AndNothingIsSent() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required: send a valid token"));

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithAnInvalidTokenReturns401() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, "Bearer not-a-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithATokenSignedWithAnotherSecretReturns401() throws Exception {
        String foreignToken = JwtTestTokens.tokenFor(JwtTestTokens.OTHER_SECRET, 24L, "EMPLOYEE");

        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, "Bearer " + foreignToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithClientTokenReturns403AndNothingIsSent() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("CLIENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not have permission to perform this action"));

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithOwnerTokenReturns403() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithAdminTokenReturns403() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(smsHandler);
    }

    @Test
    void postSmsWithEmployeeTokenReturns202AndHandsTheSmsToTheHandler() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_SMS_JSON))
                .andExpect(status().isAccepted());

        verify(smsHandler).sendSms(any(SmsRequestDto.class));
    }

    @Test
    void postSmsWithEmployeeTokenAndAnInvalidBodyPassesSecurityAndReachesTheValidation() throws Exception {
        mockMvc.perform(post(SMS_URL)
                        .header(AUTHORIZATION, bearerTokenFor("EMPLOYEE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(smsHandler);
    }
}
