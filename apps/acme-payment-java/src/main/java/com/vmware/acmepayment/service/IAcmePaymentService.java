package com.vmware.acmepayment.service;

import com.vmware.acmepayment.request.PaymentRequest;
import com.vmware.acmepayment.response.PaymentResponse;

public interface IAcmePaymentService {

    PaymentResponse processPayment(PaymentRequest paymentRequest);
}
