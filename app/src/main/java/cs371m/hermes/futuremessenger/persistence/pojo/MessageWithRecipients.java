package cs371m.hermes.futuremessenger.persistence.pojo;

import java.io.Serializable;
import java.util.List;

import cs371m.hermes.futuremessenger.persistence.entities.Message;
import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * A convenience POJO that holds a {@link Message} and its list of {@link Recipient Recipients}.
 * <p>
 * Note that this isn't persisted - it's merely a DTO used to hold all the relevant information
 * when processing a message.
 */
@EqualsAndHashCode
@Getter
@Setter
public class MessageWithRecipients implements Serializable {
    public static final String BUNDLE_KEY_MESSAGE_WITH_RECIPIENTS =
            MessageWithRecipients.class.getName();

    private static final long serialVersionUID = 1L;

    private Message message;
    private List<Recipient> recipients;

    public MessageWithRecipients(Message message, List<Recipient> recipients) {
        this.message = message;
        this.recipients = recipients;
    }
}
