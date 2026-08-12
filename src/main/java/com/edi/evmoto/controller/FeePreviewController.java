package com.edi.evmoto.controller;

import com.edi.evmoto.dto.FeePreviewRequest;
import com.edi.evmoto.dto.FeePreviewResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/orders")
public class FeePreviewController {

    @PostMapping("/{orderId}/fee-preview")
    public ResponseEntity<FeePreviewResponse> feePreview(@PathVariable("orderId") String orderId, @RequestBody FeePreviewRequest request){
        FeePreviewResponse response = null;
        return ResponseEntity.ok(response);
    }

}
