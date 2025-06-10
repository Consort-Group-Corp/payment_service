package uz.consortgroup.payment_service.event.coursepurchased;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePurchasedEvent {
    private UUID messageId;
    private UUID userId;
    private UUID courseId;
    private Instant purchasedAt;
    private Instant accessUntil;
}
