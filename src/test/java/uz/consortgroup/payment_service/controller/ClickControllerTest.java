package uz.consortgroup.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.payment_service.config.SecurityConfig;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.service.handler.click.ClickService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ClickController.class)
@Import(SecurityConfig.class)
public class ClickControllerTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClickService clickService;

    @Test
    @WithMockUser(roles = "USER")
    void handleRequest_Success() throws Exception {
        ClickRequest request = createValidRequest();

        ClickResponse response = ClickResponse.builder()
                .error(0)
                .error_note("Success")
                .click_trans_id(1L)
                .merchant_trans_id("merchant_transaction_id")
                .merchant_prepare_id("merchant_prepare_id")
                .build();

        when(clickService.handle(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void handleRequest_InvalidData_ReturnsBadRequest() throws Exception {
        ClickRequest invalidRequest = ClickRequest.builder()
                .clickTransactionId(1L)
                .serviceId(1L)
                .merchantTransactionId("invalid_transaction")
                .action(1)
                .build();

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void handleRequest_ServiceError_ReturnsInternalError() throws Exception {
        ClickRequest request = createValidRequest();

        when(clickService.handle(request))
                .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/click")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void handleRequest_Unauthorized_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/click/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OBJECT_MAPPER.writeValueAsString(createValidRequest())))
                .andExpect(status().isUnauthorized());
    }


    private ClickRequest createValidRequest() {
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
