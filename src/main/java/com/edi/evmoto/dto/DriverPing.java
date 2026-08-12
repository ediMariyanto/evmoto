package com.edi.evmoto.dto;

import java.time.OffsetDateTime;

public record DriverPing(
        OffsetDateTime at,
        double lat,
        double lng
) {
}
