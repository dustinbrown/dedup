package dedupe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Account {
    private String id;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String streetAddress;
    private Date entryDate;

    /**
     * This is overly verbose due to https://github.com/FasterXML/jackson-databind/issues/230
     * Using @JsonCreator ensures the required values are present on deserialization
     */
    @JsonCreator
    public Account(@JsonProperty(value = "_id", required = true) String id,
                   @JsonProperty(value = "email", required = true) String emailAddress,
                   @JsonProperty(value = "firstName") String firstName,
                   @JsonProperty(value = "lastName") String lastName,
                   @JsonProperty(value = "address") String streetAddress,
                   @JsonProperty(value = "entryDate", required = true) Date entryDate) {
        this.id = id;
        this.emailAddress = emailAddress;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetAddress = streetAddress;
        this.entryDate = entryDate;
    }

    public String getId() {
        return id;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public Date getEntryDate() {
        return entryDate;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", emailAddress='" + emailAddress + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", streetAddress='" + streetAddress + '\'' +
                ", entryDate=" + entryDate +
                '}';
    }
}
