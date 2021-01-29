package ru.armishev.entity;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.web.util.HtmlUtils;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.Date;
import java.util.Objects;

/*
Общая информация о файлах
 */
@Entity
@Table(name="amazon_objects")
public class AmazonObjectEntity {
    @Id
    @Column(name="key", nullable = false, unique = true)
    private String key;

    @Column(name="lastModified")
    private Date lastModified;

    @Column(name = "eTag")
    private String ETag;

    @Column(name = "size")
    @NotNull
    @Min(1)
    private Long size;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="owner", insertable = true, updatable = true)
    private OwnerEntity ownerEntity;

    @Column(name = "storageClass")
    private String storageClass;

    @ManyToMany(fetch = FetchType.EAGER, cascade={CascadeType.MERGE, CascadeType.PERSIST})
    @JoinColumn(name="grants", insertable = true, updatable = true)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<GrantEntity> grants;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REMOVE})
    @JoinColumn(name="versions", insertable = true, updatable = true)
    @Fetch(value = FetchMode.SUBSELECT)
    private List<VersionEntity> versionEntities;

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

    public String getETagUnescape() {
        return HtmlUtils.htmlUnescape(ETag);
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

    public OwnerEntity getOwner() {
        return ownerEntity;
    }

    public void setOwner(OwnerEntity ownerEntity) {
        this.ownerEntity = ownerEntity;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public List<GrantEntity> getGrants() {
        return grants;
    }

    public void setGrants(List<GrantEntity> grants) {
        this.grants = grants;
    }

    public List<VersionEntity> getVersions() {
        return versionEntities;
    }

    public void setVersions(List<VersionEntity> versionEntities) {
        this.versionEntities = versionEntities;
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
