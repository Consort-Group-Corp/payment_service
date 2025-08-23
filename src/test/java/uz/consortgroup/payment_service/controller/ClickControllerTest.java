package uz.consortgroup.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.security.ClickAuthFilter;
import uz.consortgroup.payment_service.security.PaycomAuthFilter;
import uz.consortgroup.payment_service.service.handler.click.ClickService;
import uz.consortgroup.payment_service.service.util.AuthTokenFilter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClickController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClickControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClickService clickService;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    @MockitoBean
    private ClickAuthFilter clickAuthFilter;

    @MockitoBean
    private PaycomAuthFilter paycomAuthFilter;

    @Test
    void handleRequest_Success() throws Exception {
        ClickRequest request = validRequest();

        ClickResponse response = ClickResponse.builder()
                .error(0)
                .error_note("Success")
                .click_trans_id(1L)
                .merchant_trans_id("merchant_transaction_id")
                .merchant_prepare_id("merchant_prepare_id")
                .build();

        when(clickService.handle(any(ClickRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void handleRequest_InvalidData_ReturnsBadRequest() throws Exception {
        // специально не заполняем обязательные поля (под твои @Valid)
        ClickRequest invalid = ClickRequest.builder()
                .clickTransactionId(null)
                .serviceId(null)
                .merchantTransactionId("") // пусто
                .amount(null)
                .action(null)
                .signTime(null)
                .signString(null)
                .build();

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleRequest_ServiceError_ReturnsInternalError() throws Exception {
        ClickRequest request = validRequest();

        when(clickService.handle(any(ClickRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    private static ClickRequest validRequest() {
        return ClickRequest.builder()
                .clickTransactionId(1L)
                .serviceId(1L)
                .merchantTransactionId("valid_transaction")
                .merchantPrepareId("prepare_id")
                .amount(1000L)
                .action(1)
                .signTime("2025-05-29 11:00:00")
                .signString("valid_signature")
                .build();
    }
}
