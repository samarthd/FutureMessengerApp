package cs371m.hermes.futuremessenger.persistence.pojo;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;

/**
 * POJO that holds a message and a list of recipients.
 */
public class MessageWithRecipients {
    Message message;
    List<Recipient> recipients;

    public MessageWithRecipients(Message message, List<Recipient> recipients) {
        this.message = message;
        this.recipients = recipients;
    }
}
