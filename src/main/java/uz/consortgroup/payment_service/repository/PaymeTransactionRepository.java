package uz.consortgroup.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.payment_service.entity.PaymeTransaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymeTransactionRepository extends JpaRepository<PaymeTransaction, UUID> {
    Optional<PaymeTransaction> findByPaycomTransactionId(String paycomTransactionId);
    List<PaymeTransaction> findAllByCreateTimeBetween(Instant from, Instant to);
}
