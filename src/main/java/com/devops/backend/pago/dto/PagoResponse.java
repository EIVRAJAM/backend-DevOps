package com.devops.backend.pago.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoResponse {
    private Long idPago;
    private Long idTicket;
    private String stripeChargeId;
    private String stripeRefundId;
    private BigDecimal monto;
    private String moneda;
    private String tipoPago;
    private String estadoPago;
    private LocalDateTime creadoEn;
}
