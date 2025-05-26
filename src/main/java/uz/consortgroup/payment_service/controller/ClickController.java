package uz.consortgroup.payment_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.service.handler.click.ClickService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/click")
@Validated
public class ClickController {
    private final ClickService clickService;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ClickResponse handleRequest(@RequestBody @Valid ClickRequest request) {
        return clickService.handle(request);
    }
}
