package cs371m.hermes.futuremessenger.persistence.pojo;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import lombok.EqualsAndHashCode;

/**
 * POJO that holds a message and a list of recipients.
 */
@EqualsAndHashCode
public class MessageWithRecipients {
    public Message message;
    public List<Recipient> recipients;

    public MessageWithRecipients(Message message, List<Recipient> recipients) {
        this.message = message;
        this.recipients = recipients;
    }
}
