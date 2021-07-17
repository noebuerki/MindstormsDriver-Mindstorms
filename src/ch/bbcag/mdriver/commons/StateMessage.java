package ch.bbcag.mdriver.commons;

import com.google.gson.annotations.SerializedName;

public class StateMessage extends Message {

    @SerializedName(value = "code")
    private final StateCode stateCode;

    public StateMessage(StateCode stateCode) {
        super(MessageType.STATE);
        this.stateCode = stateCode;
    }

    public StateCode getStateCode() {
        return stateCode;
    }
}
