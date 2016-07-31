package cs371m.hermes.futuremessenger;

/**
 * Created by Samarth on 7/30/2016.
 * Holds the name and phone number of a contact.
 */
public class Contact {
    private String phoneNum;
    private String name;

    public Contact (String given_name, String given_phoneNum){
        phoneNum = given_phoneNum;
        name = given_name;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && obj instanceof Contact) {
            result = (((Contact) obj).getName().equals(this.name)) &&
                    (((Contact) obj).getPhoneNum().equals(this.phoneNum));
        }
        return result;
    }
}
