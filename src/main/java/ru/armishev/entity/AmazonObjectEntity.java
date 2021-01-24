package ru.armishev.entity;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name="AmazonObject")
public class AmazonObjectEntity {
    @Id
    @Column(name="key", unique = true)
    private String key;

    @Column(name="lastModified")
    private Date lastModified;

    @Column(name = "eTag")
    private String ETag;

    @Column(name = "size")
    @NotNull
    @Min(1)
    private Long size;

    /*@ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "owner", insertable = true, updatable = true)*/
    //@OneToOne(cascade = CascadeType.ALL)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "owner", referencedColumnName = "id", insertable = true, updatable = true)
    private AmazonObjectOwnerEntity owner;

    @Column(name = "storageClass")
    private String storageClass;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getETag() {
        return ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public AmazonObjectOwnerEntity getOwner() {
        return owner;
    }

    public void setOwner(AmazonObjectOwnerEntity owner) {
        this.owner = owner;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AmazonObjectEntity that = (AmazonObjectEntity) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
