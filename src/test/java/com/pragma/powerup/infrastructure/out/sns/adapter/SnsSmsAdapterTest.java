package com.pragma.powerup.infrastructure.out.sns.adapter;

import com.pragma.powerup.domain.exception.SmsDeliveryException;
import com.pragma.powerup.domain.model.SmsModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnsSmsAdapterTest {
    private static final String PHONE = "+573005698325";
    private static final String MESSAGE = "Pedido #5 listo. Tu PIN de seguridad es 482913.";
    private static final String SMS_TYPE_ATTRIBUTE = "AWS.SNS.SMS.SMSType";

    @Mock
    private SnsClient snsClient;

    @InjectMocks
    private SnsSmsAdapter snsSmsAdapter;

    @Test
    void sendPublishesTheMessageToThePhoneAsATransactionalSms() {
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("m-1").build());

        snsSmsAdapter.send(new SmsModel(PHONE, MESSAGE));

        ArgumentCaptor<PublishRequest> requestCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(requestCaptor.capture());
        PublishRequest request = requestCaptor.getValue();
        MessageAttributeValue smsType = request.messageAttributes().get(SMS_TYPE_ATTRIBUTE);
        assertAll(
                () -> assertEquals(PHONE, request.phoneNumber()),
                () -> assertEquals(MESSAGE, request.message()),
                () -> assertEquals("String", smsType.dataType()),
                () -> assertEquals("Transactional", smsType.stringValue())
        );
    }

    @Test
    void sendWhenTheSdkCannotReachAwsThrowsSmsDeliveryExceptionWithAGenericMessage() {
        when(snsClient.publish(any(PublishRequest.class))).thenThrow(SdkClientException.create("Unable to connect"));

        SmsDeliveryException exception = assertThrows(SmsDeliveryException.class,
                () -> snsSmsAdapter.send(new SmsModel(PHONE, MESSAGE)));

        assertEquals("The SMS provider is not available", exception.getMessage());
    }

    @Test
    void sendWhenAwsRejectsTheRequestThrowsSmsDeliveryExceptionWithoutLeakingTheProviderDetail() {
        when(snsClient.publish(any(PublishRequest.class)))
                .thenThrow(SnsException.builder().message("Phone number +573005698325 is opted out").build());

        SmsDeliveryException exception = assertThrows(SmsDeliveryException.class,
                () -> snsSmsAdapter.send(new SmsModel(PHONE, MESSAGE)));

        assertEquals("The SMS provider is not available", exception.getMessage());
    }
}
