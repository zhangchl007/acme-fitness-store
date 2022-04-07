package com.vmware.acmepayment.service;

import com.vmware.acmepayment.request.PaymentRequest;
import com.vmware.acmepayment.response.PaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AcmePaymentService implements IAcmePaymentService {

    Logger logger = LoggerFactory.getLogger(AcmePaymentService.class);

    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        var success = "false";
        var status = "400";
        var message = "missing required data";
        var amount = "0";
        var transactionId = "-1";

        if (paymentRequest.cardIsNull()) {
            logger.info("payment failed due to missing card info");
        } else {
            LocalDate currentDate = LocalDate.now();
            if (paymentRequest.getCard().ccvIsNullOrEmpty() || paymentRequest.getCard().numberIsNullOrEmpty() ||
                    paymentRequest.totalIsNullOrEmpty() || paymentRequest.getCard().expYearIsNullOrEmpty() ||
                    paymentRequest.getCard().expMonthIsNullOrEmpty()) {
                logger.info("payment failed due to incomplete info");
            } else if (paymentRequest.getCard().getNumber().length() % 4 != 0) {
                logger.info("payment failed due to bad card number");
                message = "not a valid card number";
                transactionId = "-2";
            } else if (Integer.parseInt(paymentRequest.getCard().getExpYear()) < currentDate.getYear() ||
                    (Integer.parseInt(paymentRequest.getCard().getExpYear()) == currentDate.getYear() && (
                            Integer.parseInt(paymentRequest.getCard().getExpMonth()) < currentDate.getMonthValue()))) {
                logger.info("payment failed due to expired card");
                message = "card is expired";
                transactionId = "-3";
            } else {
                logger.info("payment processed successfully");
                UUID uuid = UUID.randomUUID();
                success = "true";
                status = "200";
                message = "transaction successful";
                amount = paymentRequest.getTotal();
                transactionId = uuid.toString();
            }
        }
        return new PaymentResponse(success, message, amount, transactionId, status);
    }
}
