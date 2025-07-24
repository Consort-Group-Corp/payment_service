package uz.consortgroup.payment_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.dto.click.ClickError;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickTransactionValidatorServiceImpl implements ClickTransactionValidatorService {

    @Value("${click.secret-key}")
    private String secretKey;

    private static final DateTimeFormatter SIGN_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

    @Override
    public void validateTransactionState(ClickTransaction tx, ClickTransactionState requiredState) {
        if (tx.getState() != requiredState) {
            log.warn("Invalid transaction state: expected={}, actual={}, txId={}",
                    requiredState, tx.getState(), tx.getClickTransactionId());
            throw ClickError.TRANSACTION_NOT_CONFIRMED.createException();
        }
        log.info("Transaction state is valid: state={}, txId={}", tx.getState(), tx.getClickTransactionId());
    }

    @Override
    public void validateTransactionCancelable(ClickTransaction tx) {
        if (tx.getState() == ClickTransactionState.COMPLETED) {
            log.warn("Attempt to cancel already completed transaction: txId={}", tx.getClickTransactionId());
            throw ClickError.CANNOT_CANCEL.createException();
        }
        if (tx.getState() == ClickTransactionState.CANCELED) {
            log.warn("Transaction already canceled: txId={}", tx.getClickTransactionId());
            throw ClickError.TRANSACTION_CANCELLED.createException();
        }
        log.info("Transaction is valid for cancellation: txId={}", tx.getClickTransactionId());
    }

    @Override
    public void validateSignature(ClickRequest request) {
        try {
            String signTime = request.getSignTime().trim();
            SIGN_TIME_FORMATTER.parse(signTime);

            String data = String.valueOf(request.getClickTransactionId()) +
                    request.getServiceId() +
                    secretKey +
                    request.getMerchantTransactionId() +
                    request.getAmount() +
                    request.getAction() +
                    signTime;

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(data.getBytes(StandardCharsets.UTF_8));
            String expectedSign = toHexString(digest);

            if (!expectedSign.equalsIgnoreCase(request.getSignString())) {
                log.warn("Invalid signature: expected={}, actual={}, txId={}",
                        expectedSign, request.getSignString(), request.getClickTransactionId());
                throw ClickError.SIGNATURE_ERROR.createException();
            }

            log.info("Signature is valid for txId={}", request.getClickTransactionId());
        } catch (Exception ex) {
            log.error("Error validating signature for txId={}", request.getClickTransactionId(), ex);
            throw ClickError.SIGNATURE_ERROR.createException();
        }
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
