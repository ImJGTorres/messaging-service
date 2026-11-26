package com.pragma.powerup.infrastructure.out.log.adapter;

import com.pragma.powerup.domain.model.SmsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class LoggerSmsAdapterTest {

    private final LoggerSmsAdapter loggerSmsAdapter = new LoggerSmsAdapter();

    @Test
    void sendWritesTheMessageInTheConsoleSoTheDeveloperCanSeeThePin(CapturedOutput output) {
        loggerSmsAdapter.send(new SmsModel("+573005698325", "Pedido #5 listo. Tu PIN de seguridad es 482913."));

        assertTrue(output.getOut().contains("Pedido #5 listo. Tu PIN de seguridad es 482913."));
    }

    @Test
    void sendMasksThePhoneAndOnlyShowsTheLastFourDigits(CapturedOutput output) {
        loggerSmsAdapter.send(new SmsModel("+573005698325", "Hola"));

        assertTrue(output.getOut().contains("***8325"));
        assertFalse(output.getOut().contains("+573005698325"));
        assertFalse(output.getOut().contains("573005698325"));
    }
}
