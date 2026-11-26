package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.ISmsServicePort;
import com.pragma.powerup.domain.spi.ISmsSenderPort;
import com.pragma.powerup.domain.usecase.SmsUseCase;
import com.pragma.powerup.infrastructure.out.log.adapter.LoggerSmsAdapter;
import com.pragma.powerup.infrastructure.out.sns.adapter.SnsSmsAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class BeanConfiguration {
    private static final String PROVIDER_PROPERTY = "messaging.sms.provider";

    @Bean
    @ConditionalOnProperty(name = PROVIDER_PROPERTY, havingValue = "log", matchIfMissing = true)
    public ISmsSenderPort loggerSmsSenderPort() {
        return new LoggerSmsAdapter();
    }

    @Bean
    @ConditionalOnProperty(name = PROVIDER_PROPERTY, havingValue = "sns")
    public SnsClient snsClient(@Value("${aws.region:us-east-1}") String region) {
        return SnsClient.builder().region(Region.of(region)).build();
    }

    @Bean
    @ConditionalOnProperty(name = PROVIDER_PROPERTY, havingValue = "sns")
    public ISmsSenderPort snsSmsSenderPort(SnsClient snsClient) {
        return new SnsSmsAdapter(snsClient);
    }

    @Bean
    public ISmsServicePort smsServicePort(ISmsSenderPort smsSenderPort) {
        return new SmsUseCase(smsSenderPort);
    }
}
