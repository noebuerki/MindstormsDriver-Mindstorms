package ch.noebuerki.mindstormsdriver.commons;

import com.google.gson.annotations.SerializedName;

public enum StateCode {
    @SerializedName(value = "13")
    SERVER_CLOSING,
    @SerializedName(value = "200")
    CONNECTION_OK,
    @SerializedName(value = "201")
    RECORDING_ENDED,
    @SerializedName(value = "418")
    ABORD_RECORDING
}