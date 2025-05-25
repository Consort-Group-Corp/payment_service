package uz.consortgroup.payment_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.service.PaycomService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/paycom")
@Validated
public class PaycomController {
    private final PaycomService paycomService;

    @PostMapping
    public ResponseEntity<PaycomResponse> handleRequest(@RequestBody @Valid PaycomRequest request) {
        PaycomResponse response = paycomService.handle(request);
        return ResponseEntity.ok(response);
    }

}
