package com.app.random.backapp.Receiver;


public class MessageEvent {

    public static class OnChargingDischargingEvent extends MessageEvent {
        String status;
        public OnChargingDischargingEvent (String status) {
            this.status = status;
        }
        public String getStatus() {
            return status;
        }

    }

    public static class OnFinishLoadinfIcons extends MessageEvent {
        String status;
        public OnFinishLoadinfIcons (String status) {
            this.status = status;
        }
        public String getStatus() {
            return status;
        }
    }

}
