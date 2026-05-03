package com.devops.backend.pago.repository;

import com.devops.backend.pago.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    boolean existsByStripeEventId(String stripeEventId);
}
