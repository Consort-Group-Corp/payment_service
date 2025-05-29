package uz.consortgroup.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.payment_service.entity.ClickTransaction;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClickTransactionRepository extends JpaRepository<ClickTransaction, UUID> {
    Optional<ClickTransaction> findByClickTransactionId(Long clickTransactionId);
    boolean existsByClickTransactionId(Long clickTransactionId);
}
