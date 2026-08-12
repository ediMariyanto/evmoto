package com.edi.evmoto.service;

import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WaitingFeeCalculatorService {
    Logger log = LoggerFactory.getLogger(WaitingFeeCalculatorService.class);

    private static final long FREE_WAITING_MINUTES = 5;
    private static final long FEE_PER_MINUTE = 500;
    private static final long MAX_WAITING_FEE = 15_000;
    private static final long CANCELLATION_FEE = 5_000;
    private static final long MAX_CANCELLATION_FEE = 20_000;
    private static final double MAX_PICKUP_DISTANCE_METERS = 100.0;

    public FeePreviewResponse calculate(String orderId, FeePreviewRequest feePreviewRequest) {
        log.info("calculate : {} {}", orderId, feePreviewRequest);

        boolean waitingFeeCapped = false;
        boolean cancellationFeeCapped = false;

        long waitingMinutes = 0;
        long paidWaitingMinutes = 0;
        long pausedMinutes = 0;
        long waitingFee = 0;
        long cancellationFee = 0;
        long totalFee = 0;



        return new FeePreviewResponse(
                orderId,
                waitingMinutes,
                FREE_WAITING_MINUTES,
                paidWaitingMinutes,
                pausedMinutes,
                waitingFee,
                cancellationFee,
                totalFee,
                waitingFeeCapped,
                cancellationFeeCapped
        );
    }
}
