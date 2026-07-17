package com.acmelogistics.dispatch.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "carriers")
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "contact_email", length = 160)
    private String contactEmail;

    @Column(nullable = false)
    private boolean active;

    public Carrier() {
    }

    private Carrier(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.code = builder.code;
        this.contactEmail = builder.contactEmail;
        this.active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static final class Builder {
        private Long id;
        private String name;
        private String code;
        private String contactEmail;
        private boolean active;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Carrier build() {
            return new Carrier(this);
        }
    }
}
