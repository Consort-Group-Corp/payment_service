package uz.consortgroup.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.consortgroup.payment_service.entity.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByPaycomTransactionId(String paycomTransactionId);
    List<Transaction> findAllByCreateTimeBetween(Instant from, Instant to);
}
