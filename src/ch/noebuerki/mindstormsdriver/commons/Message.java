package ch.noebuerki.mindstormsdriver.commons;

public abstract class Message {

    protected final MessageType type;

    protected Long time;

    protected Message(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime() {
        time = System.currentTimeMillis();
    }
}
