package com.vmware.acmepayment.controller;

import com.vmware.acmepayment.request.PaymentRequest;
import com.vmware.acmepayment.response.PaymentResponse;
import com.vmware.acmepayment.service.AcmePaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AcmePaymentController {

    private final AcmePaymentService acmePaymentService;

    public AcmePaymentController(AcmePaymentService acmePaymentService) {
        this.acmePaymentService = acmePaymentService;
    }

    @PostMapping("/pay")
    public PaymentResponse processPayment(@RequestBody PaymentRequest paymentRequest) {
        return acmePaymentService.processPayment(paymentRequest);
    }

}
