package com.app.random.backapp.Receiver;


public class MessageEvent {
//    private String message;

//    public MessageEvent(String message){
//        this.message = message;
//    }
//
//    public String getData(){
//        return message;
//    }
    public static class OnChargingDischargingEvent extends MessageEvent {
        String status;

        public OnChargingDischargingEvent (String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
