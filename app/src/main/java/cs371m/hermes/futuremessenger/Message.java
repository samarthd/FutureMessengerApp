package cs371m.hermes.futuremessenger;

import java.util.ArrayList;

/**
 * Created by Samarth on 8/1/2016.
 * Interface describing a message.
 */
public interface Message {
    public ArrayList<Contact> getRecipients();
    public String getMessageDate();
    public String getMessageTime();
    public String getTextContent();
    public String getDateTime();
}
