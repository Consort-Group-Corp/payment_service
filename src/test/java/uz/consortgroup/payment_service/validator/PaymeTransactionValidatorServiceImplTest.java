package uz.consortgroup.payment_service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.TransactionAlreadyCanceledException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaymeTransactionValidatorServiceImplTest {

    private PaymeTransactionValidatorServiceImpl validator;

    @BeforeEach
    void setUp() {
        validator = new PaymeTransactionValidatorServiceImpl();
    }

    @Test
    void validateTransactionState_shouldNotThrow_whenStatesMatch() {
        PaymeTransaction tx = mock(PaymeTransaction.class);
        when(tx.getState()).thenReturn(PaymeTransactionState.CREATED);

        assertDoesNotThrow(() -> validator.validateTransactionState(tx, PaymeTransactionState.CREATED));
    }

    @Test
    void validateTransactionState_shouldThrow_whenStatesDoNotMatch() {
        PaymeTransaction tx = mock(PaymeTransaction.class);
        when(tx.getState()).thenReturn(PaymeTransactionState.CANCELED);

        TransactionNotFoundException ex = assertThrows(TransactionNotFoundException.class,
                () -> validator.validateTransactionState(tx, PaymeTransactionState.CREATED));
        assertEquals("Transaction not found", ex.getMessage());
    }

    @Test
    void validateTransactionCancelable_shouldThrow_whenStateCanceled() {
        PaymeTransaction tx = mock(PaymeTransaction.class);
        when(tx.getState()).thenReturn(PaymeTransactionState.CANCELED);

        TransactionAlreadyCanceledException ex = assertThrows(TransactionAlreadyCanceledException.class,
                () -> validator.validateTransactionCancelable(tx));
        assertEquals("Transaction already canceled", ex.getMessage());
    }

    @Test
    void validateTransactionCancelable_shouldThrow_whenStateCompleted() {
        PaymeTransaction tx = mock(PaymeTransaction.class);
        when(tx.getState()).thenReturn(PaymeTransactionState.COMPLETED);

        UnableToCancelException ex = assertThrows(UnableToCancelException.class,
                () -> validator.validateTransactionCancelable(tx));
        assertEquals("Transaction already completed", ex.getMessage());
    }

    @Test
    void validateTransactionCancelable_shouldNotThrow_whenStateCreated() {
        PaymeTransaction tx = mock(PaymeTransaction.class);
        when(tx.getState()).thenReturn(PaymeTransactionState.CREATED);

        assertDoesNotThrow(() -> validator.validateTransactionCancelable(tx));
    }
}
