package ru.armishev.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name="versions")
public class VersionEntity implements Serializable {
    @Id
    @Embedded
    @Column(name="versionPK", nullable = false, unique = true)
    private VersionPK versionPK;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST})
    @JoinColumn(name="owner", insertable = true, updatable = true)
    private OwnerEntity owner;

    @Column(name = "isLatest")
    private boolean isLatest;

    @Column(name = "isDeleted")
    private boolean isDeleted;

    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    public VersionPK getVersionPK() {
        return versionPK;
    }

    public void setVersionPK(VersionPK versionPK) {
        this.versionPK = versionPK;
    }

    public OwnerEntity getOwner() {
        return owner;
    }

    public void setOwner(OwnerEntity owner) {
        this.owner = owner;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionEntity versionEntity = (VersionEntity) o;
        return versionPK.equals(versionEntity.versionPK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionPK);
    }

    @Embeddable
    public static class VersionPK implements Serializable {
        private String key;

        private String version;

        public VersionPK() {}

        public VersionPK(String key, String version) {
            this.key = key;
            this.version = version;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionPK versionPK = (VersionPK) o;
            return key.equals(versionPK.key) && version.equals(versionPK.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, version);
        }
    }
}
