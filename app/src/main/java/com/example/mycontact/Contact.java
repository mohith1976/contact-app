
package com.example.mycontact;

public class Contact {
    private String name;
    private String number;
    private String contactId;

    public Contact(String name, String number, String contactId) {
        this.name = name;
        this.number = number;
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getContactId() {
        return contactId;
    }
}
