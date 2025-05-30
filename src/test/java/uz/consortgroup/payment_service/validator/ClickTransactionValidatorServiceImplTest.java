package uz.consortgroup.payment_service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClickTransactionValidatorServiceImplTest {

    private static final String SECRET_KEY = "test_secret_key";

    private ClickTransactionValidatorServiceImpl validator;

    @BeforeEach
    void setup() throws Exception {
        validator = new ClickTransactionValidatorServiceImpl();
        java.lang.reflect.Field secretKeyField = ClickTransactionValidatorServiceImpl.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(validator, SECRET_KEY);
    }

    @Test
    void validateTransactionState_shouldThrow_whenStateDoesNotMatch() {
        ClickTransaction tx = mock(ClickTransaction.class);
        when(tx.getState()).thenReturn(ClickTransactionState.CANCELED);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> validator.validateTransactionState(tx, ClickTransactionState.CREATED));
        assertEquals("Transaction is not confirmed", ex.getMessage());
    }

    @Test
    void validateTransactionState_shouldNotThrow_whenStateMatches() {
        ClickTransaction tx = mock(ClickTransaction.class);
        when(tx.getState()).thenReturn(ClickTransactionState.CREATED);

        assertDoesNotThrow(() -> validator.validateTransactionState(tx, ClickTransactionState.CREATED));
    }

    @Test
    void validateTransactionCancelable_shouldThrowIfCompleted() {
        ClickTransaction tx = mock(ClickTransaction.class);
        when(tx.getState()).thenReturn(ClickTransactionState.COMPLETED);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> validator.validateTransactionCancelable(tx));
        assertEquals("Could not cancel transaction", ex.getMessage());
    }

    @Test
    void validateTransactionCancelable_shouldThrowIfCanceled() {
        ClickTransaction tx = mock(ClickTransaction.class);
        when(tx.getState()).thenReturn(ClickTransactionState.CANCELED);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> validator.validateTransactionCancelable(tx));
        assertEquals("Transaction already cancelled", ex.getMessage());
    }

    @Test
    void validateTransactionCancelable_shouldNotThrowIfCreated() {
        ClickTransaction tx = mock(ClickTransaction.class);
        when(tx.getState()).thenReturn(ClickTransactionState.CREATED);

        assertDoesNotThrow(() -> validator.validateTransactionCancelable(tx));
    }

    @Test
    void validateSignature_shouldPassForValidSignature() throws Exception {
        ClickRequest request = mock(ClickRequest.class);

        when(request.getClickTransactionId()).thenReturn(123L);
        when(request.getServiceId()).thenReturn(456L);
        when(request.getMerchantTransactionId()).thenReturn("merch1");
        when(request.getAmount()).thenReturn(1000L);
        when(request.getAction()).thenReturn(1);
        String signTime = "2025-05-30 12:00:00";
        when(request.getSignTime()).thenReturn(signTime);

        String data = "123" + "456" + SECRET_KEY + "merch1" + 1000L + 1 + signTime;

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(data.getBytes(StandardCharsets.UTF_8));
        String expectedSign = toHexString(digest);

        when(request.getSignString()).thenReturn(expectedSign.toUpperCase());

        assertDoesNotThrow(() -> validator.validateSignature(request));
    }

    @Test
    void validateSignature_shouldThrowForInvalidSignature() {
        ClickRequest request = mock(ClickRequest.class);

        when(request.getSignTime()).thenReturn("2025-05-30 12:00:00");
        when(request.getSignString()).thenReturn("invalidsign");
        when(request.getClickTransactionId()).thenReturn(1L);
        when(request.getServiceId()).thenReturn(456L);
        when(request.getMerchantTransactionId()).thenReturn("merchant");
        when(request.getAmount()).thenReturn(1L);
        when(request.getAction()).thenReturn(1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> validator.validateSignature(request));
        assertEquals("SIGN CHECK FAILED!", ex.getMessage());
    }

    @Test
    void validateSignature_shouldThrowForMalformedSignTime() {
        ClickRequest request = mock(ClickRequest.class);

        when(request.getSignTime()).thenReturn("invalid-time-format");
        when(request.getSignString()).thenReturn("anything");
        when(request.getClickTransactionId()).thenReturn(1L);
        when(request.getServiceId()).thenReturn(456L);
        when(request.getMerchantTransactionId()).thenReturn("merchant");
        when(request.getAmount()).thenReturn(1L);
        when(request.getAction()).thenReturn(1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> validator.validateSignature(request));
        assertEquals("SIGN CHECK FAILED!", ex.getMessage());
    }

    private String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int hi = (b >> 4) & 0xF;
            int lo = b & 0xF;
            sb.append(Character.forDigit(hi, 16))
                    .append(Character.forDigit(lo, 16));
        }
        return sb.toString();
    }
}
