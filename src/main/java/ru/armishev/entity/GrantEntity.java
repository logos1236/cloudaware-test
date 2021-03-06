package ru.armishev.entity;

import javax.persistence.*;
import java.util.Objects;

/*
Уровни доступа к файлам
 */
@Entity
@Table(name="grants")
public class GrantEntity {
    @Id
    @Column(name="key", nullable = false, unique = false)
    private String key;

    @Column(name="name")
    private String permission;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantEntity grantEntity = (GrantEntity) o;
        return key.equals(grantEntity.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
