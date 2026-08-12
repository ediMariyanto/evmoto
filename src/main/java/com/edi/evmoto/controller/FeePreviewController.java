package com.edi.evmoto.controller;

import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import com.edi.evmoto.service.FeePreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
public class FeePreviewController {


    @Autowired
    FeePreviewService feePreviewService;

    @PostMapping("/{orderId}/fee-preview")
    public ResponseEntity<FeePreviewResponse> feePreview(@PathVariable("orderId") String orderId, @RequestBody FeePreviewRequest request){
        FeePreviewResponse response = feePreviewService.feePreview(orderId, request);
        return ResponseEntity.ok(response);
    }

}
