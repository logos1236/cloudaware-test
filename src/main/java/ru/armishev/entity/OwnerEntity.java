package ru.armishev.entity;

import javax.persistence.*;
import java.util.Objects;

/*
Владельцы файлов
 */
@Entity
@Table(name="owners")
public class OwnerEntity {
    @Id
    @Column(name="key", nullable = false, unique = true)
    private String key;

    @Column(name="displayName")
    private String displayName;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
        OwnerEntity that = (OwnerEntity) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
