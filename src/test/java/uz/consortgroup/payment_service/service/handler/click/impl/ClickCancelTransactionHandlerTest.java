package uz.consortgroup.payment_service.service.handler.click.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.exception.click.ClickException;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickCancelTransactionHandlerTest {

    @Mock
    private ClickTransactionRepository repo;

    @Mock
    private ClickTransactionValidatorService validator;

    private ClickCancelTransactionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClickCancelTransactionHandler(repo, validator);
    }

    @Test
    void handle_successfulCancel_returnsZeroError() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(1L);

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(1L);
        tx.setMerchantTransactionId("mt");
        tx.setMerchantPrepareId("mp");
        tx.setState(ClickTransactionState.CREATED);

        when(repo.findByClickTransactionId(1L)).thenReturn(Optional.of(tx));
        doNothing().when(validator).validateSignature(req);
        doNothing().when(validator).validateTransactionCancelable(tx);

        ClickResponse resp = handler.handle(req);

        assertThat(resp.getError()).isZero();
        assertThat(resp.getClick_trans_id()).isEqualTo(1L);

        ArgumentCaptor<ClickTransaction> captor = ArgumentCaptor.forClass(ClickTransaction.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getState()).isEqualTo(ClickTransactionState.CANCELED);
    }

    @Test
    void handle_noTransaction_throwsNotFound() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(2L);

        when(repo.findByClickTransactionId(2L)).thenReturn(Optional.empty());
        doNothing().when(validator).validateSignature(req);

        assertThatThrownBy(() -> handler.handle(req))
                .isInstanceOf(ClickException.class)
                .hasMessage("Transaction not found");
    }

    @Test
    void handle_invalidSignature_throwsSignatureError() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(3L);

        doThrow(new ClickException(-1, Map.of("en", "SIGN CHECK FAILED")))
                .when(validator).validateSignature(req);

        assertThatThrownBy(() -> handler.handle(req))
                .isInstanceOf(ClickException.class)
                .hasMessageContaining("SIGN CHECK FAILED");
    }

    @Test
    void handle_cannotCancelCompleted_throwsCannotCancel() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(4L);

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(4L);
        tx.setState(ClickTransactionState.COMPLETED);

        when(repo.findByClickTransactionId(4L)).thenReturn(Optional.of(tx));
        doNothing().when(validator).validateSignature(req);
        doThrow(new ClickException(-11, Map.of("en", "Could not cancel transaction")))
                .when(validator).validateTransactionCancelable(tx);

        assertThatThrownBy(() -> handler.handle(req))
                .isInstanceOf(ClickException.class)
                .hasMessageContaining("Could not cancel transaction");
    }

    @Test
    void handle_cannotCancelAlreadyCanceled_throwsAlreadyCancelled() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(5L);

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(5L);
        tx.setState(ClickTransactionState.CANCELED);

        when(repo.findByClickTransactionId(5L)).thenReturn(Optional.of(tx));
        doNothing().when(validator).validateSignature(req);
        doThrow(new ClickException(-1, Map.of("en", "Transaction canceled")))
                .when(validator).validateTransactionCancelable(tx);

        assertThatThrownBy(() -> handler.handle(req))
                .isInstanceOf(ClickException.class)
                .hasMessageContaining("Transaction canceled");
    }
}
