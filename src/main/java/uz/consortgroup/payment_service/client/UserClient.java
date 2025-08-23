package uz.consortgroup.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.payment_service.config.FeignClientConfig;

import java.util.UUID;

@FeignClient(
        name = "user-service",
        contextId = "userClient",
        url = "${user.service.url}",
        configuration = FeignClientConfig.class
)
public interface UserClient {

    @GetMapping("/api/v1/internal/purchases/{userId}/courses/{courseId}/eligibility")
    EligibilityResponse checkEligibility(@PathVariable UUID userId, @PathVariable UUID courseId);
}
