package com.pragma.powerup.infrastructure.out.log.adapter;

import com.pragma.powerup.domain.model.SmsModel;
import com.pragma.powerup.domain.spi.ISmsSenderPort;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerSmsAdapter implements ISmsSenderPort {
    private static final String MASK = "***";
    private static final int VISIBLE_DIGITS = 4;

    @Override
    public void send(SmsModel smsModel) {
        log.info("SMS (provider=log, nothing was sent) to {}: {}", maskPhone(smsModel.getPhone()), smsModel.getMessage());
    }

    private String maskPhone(String phone) {
        return MASK + phone.substring(phone.length() - VISIBLE_DIGITS);
    }
}
