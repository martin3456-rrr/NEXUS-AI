package com.nexus.notification.kafka;

import com.nexus.notification.event.PaymentEvent;
import com.nexus.notification.service.NotificationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
class PaymentEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentEventListener eventListener;

    @Test
    void onPaymentEvent_shouldRetryOnFailure() {
        PaymentEvent event = PaymentEvent.builder() /* wypełnij dane */ .build();

        doThrow(new RuntimeException("Błąd bazy danych"))
                .doNothing()
                .when(notificationService).create(anyString(), anyString(), anyString());

        doThrow(new IllegalArgumentException("Błędne dane"))
                .when(notificationService).create(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> eventListener.onPaymentEvent(event));

        verify(notificationService).create(anyString(), anyString(), anyString());
    }
}