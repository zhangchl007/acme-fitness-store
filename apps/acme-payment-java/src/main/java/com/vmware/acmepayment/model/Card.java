package com.vmware.acmepayment.model;

public class Card {

    private String number;
    private String expYear;
    private String expMonth;
    private String ccv;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Boolean numberIsNullOrEmpty(){
        return this.number == null || this.number.isEmpty();
    }

    public String getExpYear() {
        return expYear;
    }

    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    public Boolean expYearIsNullOrEmpty(){
        return this.expYear  == null || this.expYear.isEmpty();
    }

    public String getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    public Boolean expMonthIsNullOrEmpty(){
        return this.expMonth  == null || this.expMonth.isEmpty();
    }

    public String getCcv() {
        return ccv;
    }

    public void setCcv(String ccv) {
        this.ccv = ccv;
    }

    public Boolean ccvIsNullOrEmpty(){
        return this.ccv  == null || this.ccv.isEmpty();
    }
}
