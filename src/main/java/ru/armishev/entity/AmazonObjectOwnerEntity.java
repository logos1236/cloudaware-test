package ru.armishev.entity;

import com.amazonaws.services.s3.model.Owner;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="AmazonObjectOwner")
public class AmazonObjectOwnerEntity {
    @Id
    @Column(name="id", unique = true)
    private String id;

    @Column(name="displayName")
    private String displayName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmazonObjectOwnerEntity that = (AmazonObjectOwnerEntity) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
