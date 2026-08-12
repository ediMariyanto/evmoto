package com.edi.evmoto.service;

import com.edi.evmoto.dto.DriverPing;
import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import com.edi.evmoto.dto.PickupPoint;
import com.edi.evmoto.model.EndReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class WaitingCalculatorTest {

    private WaitingFeeCalculatorService waitingFeeCalculatorService;

    private static final double PICKUP_LAT = -6.208800;
    private static final double PICKUP_LNG = 106.845600;

    @BeforeEach
    void setUp() {
        waitingFeeCalculatorService = new WaitingFeeCalculatorService();
    }

    @Test
    void shouldHaveNoFeeWhenWaitingLessThanFreeWaitingTime() {

        FeePreviewRequest request = new FeePreviewRequest(
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(3),
                EndReason.TRIP_STARTED,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                OffsetDateTime.now(),
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );


        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate("ORD-001", request);

        assertThat(response.waitingMinutes()).isEqualTo(3);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isZero();
        assertThat(response.waitingFee()).isZero();
        assertThat(response.totalFee()).isZero();
        assertThat(response.waitingFeeCapped()).isFalse();
    }

    @Test
    void shouldHaveNoFeeWhenWaitingExactlyFiveMinutes() {

        OffsetDateTime arrivedAt =
                OffsetDateTime.parse("2026-08-12T14:00:00+07:00");

        FeePreviewRequest request = new FeePreviewRequest(
                arrivedAt,
                arrivedAt.plusMinutes(5),
                EndReason.TRIP_STARTED,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                arrivedAt,
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );

        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate(
                        "ORD-002",
                        request
                );

        assertThat(response.waitingMinutes()).isEqualTo(5);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isZero();
        assertThat(response.waitingFee()).isZero();
        assertThat(response.totalFee()).isZero();
        assertThat(response.waitingFeeCapped()).isFalse();
    }


    @Test
    void shouldHaveFeeWhenWaitingFiveMinutesAndOneSecond() {

        OffsetDateTime arrivedAt =
                OffsetDateTime.parse("2026-08-12T14:00:00+07:00");

        FeePreviewRequest request = new FeePreviewRequest(
                arrivedAt,
                arrivedAt.plusMinutes(5).plusSeconds(1),
                EndReason.TRIP_STARTED,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                arrivedAt,
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );

        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate(
                        "ORD-002",
                        request
                );

        assertThat(response.waitingMinutes()).isEqualTo(6);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isEqualTo(1);
        assertThat(response.waitingFee()).isEqualTo(500);
        assertThat(response.totalFee()).isEqualTo(500);
        assertThat(response.waitingFeeCapped()).isFalse();
    }

    @Test
    void shouldHaveFeeWhenWaitingTenMinutes() {

        OffsetDateTime arrivedAt =
                OffsetDateTime.parse("2026-08-12T14:00:00+07:00");

        FeePreviewRequest request = new FeePreviewRequest(
                arrivedAt,
                arrivedAt.plusMinutes(10),
                EndReason.TRIP_STARTED,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                arrivedAt,
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );

        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate(
                        "ORD-002",
                        request
                );

        assertThat(response.waitingMinutes()).isEqualTo(10);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isEqualTo(5);
        assertThat(response.waitingFee()).isEqualTo(2500);
        assertThat(response.totalFee()).isEqualTo(2500);
        assertThat(response.waitingFeeCapped()).isFalse();
    }

    @Test
    void shouldHaveFeeWhenWaitingFortyMinutes() {

        OffsetDateTime arrivedAt =
                OffsetDateTime.parse("2026-08-12T14:00:00+07:00");

        FeePreviewRequest request = new FeePreviewRequest(
                arrivedAt,
                arrivedAt.plusMinutes(40),
                EndReason.TRIP_STARTED,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                arrivedAt,
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );

        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate(
                        "ORD-002",
                        request
                );

        assertThat(response.waitingMinutes()).isEqualTo(40);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isEqualTo(35);
        assertThat(response.waitingFee()).isEqualTo(15000);
        assertThat(response.totalFee()).isEqualTo(15000);
        assertThat(response.waitingFeeCapped()).isTrue();
    }


    @Test
    void shouldHaveNoFeeWhenCustomerCancelLessThanFreeWaitingTime() {

        FeePreviewRequest request = new FeePreviewRequest(
                OffsetDateTime.now(),
                OffsetDateTime.now().plusMinutes(3),
                EndReason.CANCELLED_BY_CUSTOMER,

                new PickupPoint(
                        PICKUP_LAT,
                        PICKUP_LNG
                ),

                List.of(
                        new DriverPing(
                                OffsetDateTime.now(),
                                PICKUP_LAT,
                                PICKUP_LNG
                        )
                )
        );


        FeePreviewResponse response =
                waitingFeeCalculatorService.calculate("ORD-001", request);

        assertThat(response.waitingMinutes()).isEqualTo(3);
        assertThat(response.freeWaitingMinutes()).isEqualTo(5);
        assertThat(response.paidWaitingMinutes()).isZero();
        assertThat(response.waitingFee()).isZero();
        assertThat(response.totalFee()).isZero();
        assertThat(response.waitingFeeCapped()).isFalse();
    }

}