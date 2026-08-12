package com.edi.evmoto.service;

import com.edi.evmoto.dto.DriverPing;
import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import com.edi.evmoto.model.EndReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WaitingFeeCalculatorService {
    Logger log = LoggerFactory.getLogger(WaitingFeeCalculatorService.class);

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private static final long FREE_WAITING_MINUTES = 5;
    private static final long FEE_PER_MINUTE = 500;
    private static final long MAX_WAITING_FEE = 15_000;
    private static final long CANCELLATION_FEE = 5_000;
    private static final long MAX_CANCELLATION_FEE = 20_000;
    private static final double MAX_PICKUP_DISTANCE_METERS = 100.0;

    public FeePreviewResponse calculate(String orderId, FeePreviewRequest feePreviewRequest) /*throws Exception*/ {
        log.info("calculate : {} {}", orderId, feePreviewRequest);

        boolean waitingFeeCapped = false;
        boolean cancellationFeeCapped = false;

        long waitingMinutes = 0;
        long paidWaitingMinutes = 0;
        long pausedMinutes = 0;
        long waitingFee = 0;
        long cancellationFee = 0;
        long totalFee = 0;

        if (feePreviewRequest.endReason().equals(EndReason.CANCELLED_BY_CUSTOMER) || feePreviewRequest.endReason().equals(EndReason.TRIP_STARTED)) {

            List<DriverPing> pings =
                    feePreviewRequest.driverPings()
                            .stream()
                            .sorted(Comparator.comparing(DriverPing::at))
                            .toList();

            log.debug("pings: {}", pings);

            long activeSeconds = 0;
            long pausedSeconds = 0;

            OffsetDateTime currentTime = feePreviewRequest.arrivedAt();
            log.debug("currentTime: {}", currentTime);

            boolean isActive = true;

            for (DriverPing driverPing : pings) {
                log.debug("driverPing: {}", driverPing);

                OffsetDateTime pingTime = driverPing.at();

                long seconds = Duration.between(currentTime, pingTime).getSeconds();
                log.debug("seconds: {}", seconds);

                isActive = isWithinRadius(feePreviewRequest.pickupPoint().lat(), feePreviewRequest.pickupPoint().lng(), driverPing.lat(), driverPing.lng(), MAX_PICKUP_DISTANCE_METERS);
                log.debug("isActive: {}", isActive);
                if (isActive) {
                    activeSeconds += seconds;
                } else {
                    pausedSeconds += seconds;
                }
                currentTime = pingTime;

                log.debug("currentTime: {}", currentTime);
                log.debug("seconds: {} {}", activeSeconds, pausedSeconds);
            }

            if (currentTime.isBefore(feePreviewRequest.endedAt())) {
                long remainingSeconds = Duration.between(
                        currentTime,
                        feePreviewRequest.endedAt()
                ).getSeconds();

                if (isActive) {
                    activeSeconds += remainingSeconds;
                } else {
                    pausedSeconds += remainingSeconds;
                }
            }
            log.debug("SecondsFinal: {} {}", activeSeconds, pausedSeconds);

            int activeMinutes = (int) ((activeSeconds + 59) / 60);
            int pauseMinutes = (int) ((pausedSeconds + 59) / 60);

            log.debug("Minutes: {} {} ", activeMinutes,  pauseMinutes);

            waitingMinutes = activeMinutes;
            pausedMinutes = pauseMinutes;

            paidWaitingMinutes = Math.max(waitingMinutes - FREE_WAITING_MINUTES, 0);
            log.debug("paidWaitingMinutes: {}", paidWaitingMinutes);

            waitingFee = Math.min((paidWaitingMinutes * FEE_PER_MINUTE), MAX_WAITING_FEE );
            log.debug("waitingFee: {}", waitingFee);

            totalFee = waitingFee;

            if (feePreviewRequest.endReason().equals(EndReason.CANCELLED_BY_CUSTOMER)) {
                long waitingCancelFee = paidWaitingMinutes * FEE_PER_MINUTE;
                if (waitingCancelFee > 0) {
                    cancellationFee = CANCELLATION_FEE;
                    long cancelWithWaitingFee = cancellationFee + waitingFee;
                    totalFee = Math.min(cancelWithWaitingFee , MAX_CANCELLATION_FEE );

                    if (cancelWithWaitingFee == MAX_CANCELLATION_FEE)
                        cancellationFeeCapped = true;
                }
            }
        }

        if (waitingFee == MAX_WAITING_FEE) {
            waitingFeeCapped = true;
        }

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

    public double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        log.debug("calculate distanceInMeters : {} {} {} {}", lat1, lon1, lat2, lon2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        log.debug("print c : {}", c);

        double result = EARTH_RADIUS_METERS * c;
        log.debug("result : {}", result);

        return result;
    }

    public boolean isWithinRadius(double lat1, double lon1, double lat2, double lon2, double radiusInMeters) {
        return calculateDistanceInMeters(lat1, lon1, lat2, lon2) <= radiusInMeters;
    }

}
