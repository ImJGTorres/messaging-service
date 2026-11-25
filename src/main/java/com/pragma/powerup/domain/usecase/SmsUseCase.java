package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.ISmsServicePort;
import com.pragma.powerup.domain.exception.InvalidSmsException;
import com.pragma.powerup.domain.model.SmsModel;
import com.pragma.powerup.domain.spi.ISmsSenderPort;

public class SmsUseCase implements ISmsServicePort {
    private static final int MAX_MESSAGE_LENGTH = 160;
    private static final String INVALID_MESSAGE_LENGTH = "The message must have between 1 and 160 characters";

    private final ISmsSenderPort smsSenderPort;

    public SmsUseCase(ISmsSenderPort smsSenderPort) {
        this.smsSenderPort = smsSenderPort;
    }

    @Override
    public void sendSms(SmsModel smsModel) {
        validateMessage(smsModel.getMessage());
        smsSenderPort.send(smsModel);
    }

    private void validateMessage(String message) {
        if (message == null || message.isBlank() || message.length() > MAX_MESSAGE_LENGTH) {
            throw new InvalidSmsException(INVALID_MESSAGE_LENGTH);
        }
    }
}
