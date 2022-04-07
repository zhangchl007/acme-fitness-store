package com.vmware.acmepayment.request;

import com.vmware.acmepayment.model.Card;

public class PaymentRequest {
    private Card card;
    private String total;

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Boolean cardIsNull(){
        return this.card == null;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Boolean totalIsNullOrEmpty(){
        return this.total == null || this.total.isEmpty();
    }

}
