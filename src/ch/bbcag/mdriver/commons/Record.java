package ch.bbcag.mdriver.commons;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Record extends Message {

    @SerializedName(value = "date")
    private Date saveDate;

    private final List<Message> messages = new ArrayList<>();


    public Record() {
        super(MessageType.RECORD);
    }

    public void add(Message e) {
        e.setTime();
        messages.add(e);
    }

    public void endRecord() {
        saveDate = new Date();
        setTime();
    }

    public Date getSaveDate() {
        return saveDate;
    }


    public List<Message> getMessages() {
        return messages;
    }

}
