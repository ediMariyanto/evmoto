package com.edi.evmoto.dto;

public record FeePreviewResponse(
        String orderId,
        long waitingMinutes,
        long freeWaitingMinutes,
        long paidWaitingMinutes,
        long pausedMinutes,
        long waitingFee,
        long cancellationFee,
        long totalFee,
        boolean waitingFeeCapped,
        boolean cancellationFeeCapped
) {
}
