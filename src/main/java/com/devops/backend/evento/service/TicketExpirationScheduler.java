package com.devops.backend.evento.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketExpirationScheduler {

    private final TicketCheckoutExpirationService expirationService;

    @Value("${tickets.checkout.expiration-batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${tickets.checkout.expiration-scan-ms:300000}")
    public void expirarTicketsPendientes() {
        int actualizados = expirationService.resolverTicketsPendientesVencidos(batchSize);

        if (actualizados > 0) {
            log.info("Tickets pendientes vencidos resueltos: {}", actualizados);
        }
    }
}
