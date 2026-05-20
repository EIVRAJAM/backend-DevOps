package com.devops.backend.pago.dto;

public record RefundEmailModel(
        String title,
        String preheader,
        String eventName,
        String amount,
        String status,
        String date,
        String mainMessage,
        String organizerComment,
        String badgeBg,
        String badgeColor
) {
}
