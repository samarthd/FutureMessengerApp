package cs371m.hermes.futuremessenger.persistence.pojo;

import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * POJO that holds a message and a list of recipients.
 */
@EqualsAndHashCode
@Getter
@Setter
public class MessageWithRecipients {
    private Message message;
    private List<Recipient> recipients;

    public MessageWithRecipients(Message message, List<Recipient> recipients) {
        this.message = message;
        this.recipients = recipients;
    }
}
