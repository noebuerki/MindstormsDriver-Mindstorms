package ch.bbcag.mdriver.commons;

import com.google.gson.annotations.SerializedName;

public class DriveMessage extends Message {

    @SerializedName(value = "l")
    private float leftSpeed;

    @SerializedName(value = "r")
    private float rightSpeed;

    public DriveMessage(float leftSpeed, float rightSpeed) {
        super(MessageType.DRIVE);
        this.leftSpeed = leftSpeed;
        this.rightSpeed = rightSpeed;
    }

    public void increaseRightSpeed(float speed) {
        rightSpeed += speed;
    }

    public void increaseLeftSpeed(float speed) {
        leftSpeed += speed;
    }

    public float getLeft() {
        return leftSpeed;
    }

    public float getRight() {
        return rightSpeed;
    }

}
