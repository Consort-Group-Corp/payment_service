package uz.consortgroup.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.payment_service.config.SecurityConfig;
import uz.consortgroup.payment_service.dto.paycom.PaycomError;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.service.handler.payme.PaycomService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaycomController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaycomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaycomService paycomService;

    private String basicAuthHeader;

    @BeforeEach
    void setup() {
        String username = "login";
        String password = "password";
        String creds = username + ":" + password;
        basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void handleRequest_Success() throws Exception {
        PaycomRequest request = PaycomRequest.builder()
                .id(1L)
                .method("testMethod")
                .params(Map.of("key", "value"))
                .build();

        PaycomResponse response = PaycomResponse.builder()
                .id(1L)
                .result("result")
                .build();

        when(paycomService.handle(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/paycom")
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void handleRequest_InvalidJson_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{ invalid }";

        mockMvc.perform(post("/api/v1/paycom")
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleRequest_MissingFields_ShouldReturnBadRequest() throws Exception {
        String jsonMissingFields = "{}";

        mockMvc.perform(post("/api/v1/paycom")
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMissingFields))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleRequest_ErrorResponse_ShouldReturnOkWithError() throws Exception {
        PaycomRequest request = PaycomRequest.builder()
                .id(1L)
                .method("nonExistentMethod")
                .params(Map.of())
                .build();

        PaycomResponse errorResponse = PaycomResponse.builder()
                .id(1L)
                .error(PaycomError.builder()
                        .code(-32601)
                        .message(Map.of("ru", "Метод не найден", "en", "Method not found"))
                        .build())
                .build();

        when(paycomService.handle(request)).thenReturn(errorResponse);

        mockMvc.perform(post("/api/v1/paycom")
                        .header("Authorization", basicAuthHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
