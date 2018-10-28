package cs371m.hermes.futuremessenger.persistence.pojo;

import java.io.Serializable;
import java.util.SortedMap;

import cs371m.hermes.futuremessenger.persistence.entities.Recipient;
import cs371m.hermes.futuremessenger.persistence.entities.embedded.Status;
import cs371m.hermes.futuremessenger.persistence.typeconverters.StatusDetailsConverter;
import lombok.Data;


/**
 * While this may seem icky, I am storing the results of sending all parts of a message
 * (as there may be more than 1 part if a message is longer than 160 chars or there
 * is more than one recipient) in a single cell. This value is converted between a serialized blob
 * for storage in one cell of a table, to a collection when being manipulated in memory.
 * <p>
 * So for example, if I was to send the message "Hello" to Bob and Alice, the success/failure
 * of sending "Hello" to Bob and "Hello" to Alice would both be put in this single field.
 * <p>
 * The fundamental reason for doing this is to avoid complicating the regular domain where
 * message fragmentation doesn't really make sense. We just want to store 1 message that
 * represents all of the text content and information about the message. We don't want to have
 * an entire table dedicated to message_parts, where we would need a row for
 * each message chunk of 160 chars being sent to each recipient...too icky.
 * <p>
 * Message fragmentation is only relevant in describing the results after the message is divided
 * for sending -- thus we will only use it in that manner.
 *
 * @see StatusDetailsConverter
 * @see Status#details
 */
@Data
public class StatusDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * A map of <Recipient, SortedMap<MessagePartIndex, SentResultCode>>
     */
    private SortedMap<Recipient, SortedMap<Integer, Integer>> detailsMap;

    /**
     * The total number of message parts that will be sent to any one recipient
     */
    private int totalMessagePartCountForEachRecipient;

}
