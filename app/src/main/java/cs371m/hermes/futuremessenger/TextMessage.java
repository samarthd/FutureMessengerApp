package cs371m.hermes.futuremessenger;

import java.util.ArrayList;

/**
 * Represents a text message.
 * Created by Samarth on 8/1/2016.
 */
public class TextMessage implements Message {

    private ArrayList<Contact> recipients;
    private String date;
    private String time;
    private String textContent;
    private String dateTime;

    public TextMessage(ArrayList<Contact> given_recips, String date, String time,
                       String textContent, String dateTime) {
        this.recipients = given_recips;
        this.date = date;
        this.time = time;
        this.textContent = textContent;
        this.dateTime = dateTime;
    }
    @Override
    public ArrayList<Contact> getRecipients() {
        return new ArrayList<>(recipients);
    }

    @Override
    public String getMessageDate() {
        return date;
    }

    @Override
    public String getMessageTime() {
        return time;
    }

    @Override
    public String getTextContent() {
        return textContent;
    }

    @Override
    public String getDateTime() {
        return dateTime;
    }
}
