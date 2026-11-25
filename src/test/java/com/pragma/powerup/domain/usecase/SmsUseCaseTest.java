package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.InvalidSmsException;
import com.pragma.powerup.domain.exception.SmsDeliveryException;
import com.pragma.powerup.domain.model.SmsModel;
import com.pragma.powerup.domain.spi.ISmsSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SmsUseCaseTest {
    private static final String PHONE = "+573005698325";
    private static final int MAX_LENGTH = 160;

    @Mock
    private ISmsSenderPort smsSenderPort;

    @InjectMocks
    private SmsUseCase smsUseCase;

    private SmsModel buildSms(String message) {
        return new SmsModel(PHONE, message);
    }

    @Test
    void sendSmsHandsTheSmsToTheSenderPort() {
        SmsModel sms = buildSms("Pedido #5 listo en Arepas. Tu PIN de seguridad es 482913.");

        smsUseCase.sendSms(sms);

        verify(smsSenderPort).send(sms);
    }

    @Test
    void sendSmsWithAMessageOfExactlyTheMaximumLengthIsAccepted() {
        SmsModel sms = buildSms("a".repeat(MAX_LENGTH));

        smsUseCase.sendSms(sms);

        verify(smsSenderPort).send(sms);
    }

    @Test
    void sendSmsWithAMessageOneCharacterOverTheLimitThrowsInvalidSmsExceptionBecauseItWouldCostTwoSegments() {
        SmsModel sms = buildSms("a".repeat(MAX_LENGTH + 1));

        InvalidSmsException exception = assertThrows(InvalidSmsException.class, () -> smsUseCase.sendSms(sms));

        assertEquals("The message must have between 1 and 160 characters", exception.getMessage());
        verify(smsSenderPort, never()).send(any());
    }

    @Test
    void sendSmsWithABlankMessageThrowsInvalidSmsException() {
        assertThrows(InvalidSmsException.class, () -> smsUseCase.sendSms(buildSms("   ")));

        verify(smsSenderPort, never()).send(any());
    }

    @Test
    void sendSmsWithoutMessageThrowsInvalidSmsException() {
        assertThrows(InvalidSmsException.class, () -> smsUseCase.sendSms(buildSms(null)));

        verify(smsSenderPort, never()).send(any());
    }

    @Test
    void sendSmsWhenTheProviderFailsLetsTheDeliveryExceptionPass() {
        SmsModel sms = buildSms("Hola");
        doThrow(new SmsDeliveryException("The SMS provider is not available")).when(smsSenderPort).send(sms);

        SmsDeliveryException exception = assertThrows(SmsDeliveryException.class, () -> smsUseCase.sendSms(sms));

        assertEquals("The SMS provider is not available", exception.getMessage());
    }
}
