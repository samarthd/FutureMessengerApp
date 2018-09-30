package cs371m.hermes.futuremessenger.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cs371m.hermes.futuremessenger.persistence.pojo.StatusDetails;

/**
 * @see StatusDetails
 */
public class StatusDetailsConverter {

    @SuppressWarnings("unchecked")
    @TypeConverter
    public StatusDetails deserializeDetails(byte[] serializedMap) {
        if (serializedMap == null) {
            return null;
        }
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedMap);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (StatusDetails) objectInputStream.readObject();
        } catch (Exception e) {
            return null;
        }
    }

    @TypeConverter
    public byte[] serializeDetails(StatusDetails input) {
        if (input == null) {
            return null;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(input);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
