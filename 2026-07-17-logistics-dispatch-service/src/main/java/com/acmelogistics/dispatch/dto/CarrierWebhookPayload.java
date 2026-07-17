package com.acmelogistics.dispatch.dto;

public class CarrierWebhookPayload {
    private String trackingNumber;
    private String carrierCode;
    private String eventCode;
    private String rawTimestamp;

    public CarrierWebhookPayload() {
    }

    public CarrierWebhookPayload(String trackingNumber, String carrierCode, String eventCode, String rawTimestamp) {
        this.trackingNumber = trackingNumber;
        this.carrierCode = carrierCode;
        this.eventCode = eventCode;
        this.rawTimestamp = rawTimestamp;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getRawTimestamp() {
        return rawTimestamp;
    }

    public void setRawTimestamp(String rawTimestamp) {
        this.rawTimestamp = rawTimestamp;
    }
}
