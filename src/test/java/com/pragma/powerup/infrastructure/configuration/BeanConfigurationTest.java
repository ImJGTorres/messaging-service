package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.ISmsServicePort;
import com.pragma.powerup.domain.spi.ISmsSenderPort;
import com.pragma.powerup.infrastructure.out.log.adapter.LoggerSmsAdapter;
import com.pragma.powerup.infrastructure.out.sns.adapter.SnsSmsAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.services.sns.SnsClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la regla que protege el dinero: sin configurar nada, el proveedor es "log" y nunca se crea
 * un cliente de AWS. Solo con messaging.sms.provider=sns se usa SNS.
 */
class BeanConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BeanConfiguration.class);

    @Test
    void withoutAnyConfigurationTheLogProviderIsUsedAndNoAwsClientIsCreated() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ISmsSenderPort.class);
            assertThat(context.getBean(ISmsSenderPort.class)).isInstanceOf(LoggerSmsAdapter.class);
            assertThat(context).doesNotHaveBean(SnsClient.class);
            assertThat(context).hasSingleBean(ISmsServicePort.class);
        });
    }

    @Test
    void withTheLogProviderTheSmsIsOnlyWrittenInTheConsole() {
        contextRunner.withPropertyValues("messaging.sms.provider=log").run(context -> {
            assertThat(context.getBean(ISmsSenderPort.class)).isInstanceOf(LoggerSmsAdapter.class);
            assertThat(context).doesNotHaveBean(SnsClient.class);
        });
    }

    @Test
    void onlyWithTheSnsProviderTheAwsClientAndTheSnsAdapterAreCreated() {
        contextRunner.withPropertyValues("messaging.sms.provider=sns", "aws.region=us-east-1").run(context -> {
            assertThat(context).hasSingleBean(SnsClient.class);
            assertThat(context.getBean(ISmsSenderPort.class)).isInstanceOf(SnsSmsAdapter.class);
        });
    }

    @Test
    void anUnknownProviderMakesTheApplicationFailToStartInsteadOfSilentlyChoosingOne() {
        contextRunner.withPropertyValues("messaging.sms.provider=twilio")
                .run(context -> assertThat(context).hasFailed());
    }
}
