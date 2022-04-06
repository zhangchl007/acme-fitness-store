package com.vmware.acmepayment.service;

import com.vmware.acmepayment.request.PaymentRequest;
import com.vmware.acmepayment.response.PaymentResponse;
import org.springframework.stereotype.Service;

@Service
public class AcmePaymentService implements IAcmePaymentService {
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        // translate cases!
        return null;
    }
}
