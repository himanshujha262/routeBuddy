package com.routbuddy.users.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.routbuddy.common.domain.entity.BaseEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Column(name = "is_primary", nullable = false)
    private boolean primaryContact = false;

    public EmergencyContact() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private User user;
        private String contactName;
        private String contactPhone;
        private String relationship;
        private boolean primaryContact = false;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder contactName(String contactName) {
            this.contactName = contactName;
            return this;
        }

        public Builder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public Builder relationship(String relationship) {
            this.relationship = relationship;
            return this;
        }

        public Builder primaryContact(boolean primaryContact) {
            this.primaryContact = primaryContact;
            return this;
        }

        public EmergencyContact build() {
            EmergencyContact contact = new EmergencyContact();
            if (id != null) contact.setId(id);
            contact.setUser(user);
            contact.setContactName(contactName);
            contact.setContactPhone(contactPhone);
            contact.setRelationship(relationship);
            contact.setPrimaryContact(primaryContact);
            return contact;
        }
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public boolean isPrimaryContact() { return primaryContact; }
    public void setPrimaryContact(boolean primaryContact) { this.primaryContact = primaryContact; }
}
