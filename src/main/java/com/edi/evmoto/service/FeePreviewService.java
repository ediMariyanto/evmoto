package com.edi.evmoto.service;

import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeePreviewService {

    @Autowired
    WaitingFeeCalculatorService waitingFeeCalculatorService;

    public FeePreviewResponse feePreview(String orderId,FeePreviewRequest request) {
        return waitingFeeCalculatorService.calculate(orderId, request);
    }
}
