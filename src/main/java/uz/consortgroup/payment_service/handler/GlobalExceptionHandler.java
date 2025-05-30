package uz.consortgroup.payment_service.handler;

import jakarta.el.MethodNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uz.consortgroup.payment_service.exception.AmountMismatchException;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.exception.OrderInvalidStatusException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.TransactionAlreadyCanceledException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;

import java.text.ParseException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(AmountMismatchException.class)
    public ResponseEntity<ErrorResponse> handleAmountMismatchException(AmountMismatchException e) {
        log.error("AmountMismatchException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "amount_mismatch", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e) {
        log.error("OrderNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "order_not_found", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(OrderInvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleOrderInvalidStatusException(OrderInvalidStatusException e) {
        log.error("OrderInvalidStatusException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "order_invalid_status", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleOrderAlreadyExistsException(OrderAlreadyExistsException e) {
        log.error("OrderAlreadyExistsException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "order_already_exists", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(TransactionAlreadyCanceledException.class)
    public ResponseEntity<ErrorResponse> handleTransactionAlreadyCanceledException(TransactionAlreadyCanceledException e) {
        log.error("TransactionAlreadyCanceledException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "transaction_already_canceled", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(TransactionNotFoundException e) {
        log.error("TransactionNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "transaction_not_found", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(UnableToCancelException.class)
    public ResponseEntity<ErrorResponse> handlePaycomUnableToCancelException(UnableToCancelException e) {
        log.error("Paycom UnableToCancelException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "paycom_unable_to_cancel", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotFoundException(MethodNotFoundException e) {
        log.error("MethodNotFoundException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "method_not_found", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ParseException.class)
    public ResponseEntity<ErrorResponse> handleParseException(ParseException e) {
        log.error("ParseException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "parse_error", "Ошибка парсинга данных: " + e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "illegal_state", e.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "internal_server_error", "Произошла непредвиденная ошибка"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "bad_request", "Неправильный запрос"),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "validation_error", errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }
}
