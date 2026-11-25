package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.application.dto.request.SmsRequestDto;
import com.pragma.powerup.application.mapper.ISmsRequestMapper;
import com.pragma.powerup.domain.api.ISmsServicePort;
import com.pragma.powerup.domain.exception.InvalidSmsException;
import com.pragma.powerup.domain.model.SmsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsHandlerTest {

    @Mock
    private ISmsServicePort smsServicePort;

    @Mock
    private ISmsRequestMapper smsRequestMapper;

    @InjectMocks
    private SmsHandler smsHandler;

    @Test
    void sendSmsMapsTheRequestAndHandsTheSmsToTheServicePort() {
        SmsRequestDto request = new SmsRequestDto();
        SmsModel sms = new SmsModel("+573005698325", "Hola");
        when(smsRequestMapper.toSms(request)).thenReturn(sms);

        smsHandler.sendSms(request);

        verify(smsServicePort).sendSms(sms);
    }

    @Test
    void sendSmsWhenTheServicePortRejectsTheSmsLetsTheExceptionPass() {
        SmsRequestDto request = new SmsRequestDto();
        SmsModel sms = new SmsModel("+573005698325", "a".repeat(161));
        when(smsRequestMapper.toSms(request)).thenReturn(sms);
        doThrow(new InvalidSmsException("The message must have between 1 and 160 characters"))
                .when(smsServicePort).sendSms(sms);

        assertThrows(InvalidSmsException.class, () -> smsHandler.sendSms(request));
    }
}
