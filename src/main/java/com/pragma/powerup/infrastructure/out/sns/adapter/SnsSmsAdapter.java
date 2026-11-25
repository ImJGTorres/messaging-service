package com.pragma.powerup.infrastructure.out.sns.adapter;

import com.pragma.powerup.domain.exception.SmsDeliveryException;
import com.pragma.powerup.domain.model.SmsModel;
import com.pragma.powerup.domain.spi.ISmsSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class SnsSmsAdapter implements ISmsSenderPort {
    private static final String SMS_TYPE_ATTRIBUTE = "AWS.SNS.SMS.SMSType";
    private static final String TRANSACTIONAL = "Transactional";
    private static final String STRING_DATA_TYPE = "String";
    private static final String PROVIDER_UNAVAILABLE_MESSAGE = "The SMS provider is not available";

    private final SnsClient snsClient;

    @Override
    public void send(SmsModel smsModel) {
        PublishRequest request = PublishRequest.builder()
                .phoneNumber(smsModel.getPhone())
                .message(smsModel.getMessage())
                .messageAttributes(Map.of(SMS_TYPE_ATTRIBUTE, MessageAttributeValue.builder()
                        .dataType(STRING_DATA_TYPE)
                        .stringValue(TRANSACTIONAL)
                        .build()))
                .build();
        try {
            PublishResponse response = snsClient.publish(request);
            log.info("SMS accepted by SNS, messageId={}", response.messageId());
        } catch (SdkException exception) {
            log.error("SNS could not send the SMS: {}", exception.getMessage());
            throw new SmsDeliveryException(PROVIDER_UNAVAILABLE_MESSAGE);
        }
    }
}
