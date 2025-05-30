package uz.consortgroup.payment_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uz.consortgroup.payment_service.config.SecurityConfig;
import uz.consortgroup.payment_service.dto.order.OrderRequest;
import uz.consortgroup.payment_service.dto.order.OrderResponse;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.OrderStatus;
import uz.consortgroup.payment_service.service.order.OrderService;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void create_success() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .externalOrderId("123")
                .amount(15000L)
                .source(OrderSource.CLICK)
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(UUID.randomUUID())
                .externalOrderId("123")
                .amount(15000L)
                .source(OrderSource.CLICK)
                .status(OrderStatus.NEW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_shouldReturn400_whenExternalOrderIdIsMissing() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .externalOrderId("") // blank
                .amount(15000L)
                .source(uz.consortgroup.payment_service.entity.OrderSource.CLICK)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenAmountIsTooSmall() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .externalOrderId("abc123")
                .amount(10L)
                .source(uz.consortgroup.payment_service.entity.OrderSource.CLICK)
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenSourceIsMissing() throws Exception {
        OrderRequest request = OrderRequest.builder()
                .externalOrderId("abc123")
                .amount(15000L)
                .source(null) // null
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}