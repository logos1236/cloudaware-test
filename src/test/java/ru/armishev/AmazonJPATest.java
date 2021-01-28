package ru.armishev;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;
import ru.armishev.service.AmazonEntitySyncTestMock;

import java.util.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class AmazonJPATest {
    private final AmazonObjectJPA amazonObjectJPA;
    private final AmazonEntitySyncTestMock amazonEntity;

    @BeforeAll
    public void clearDatabase() {
        amazonObjectJPA.deleteAll();
    }

    @Autowired
    public AmazonJPATest(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
        this.amazonEntity = new AmazonEntitySyncTestMock(amazonObjectJPA);
    }

    @Test
    public void emptyDatabaseTest() {
        List<AmazonObjectEntity> databaseList = amazonObjectJPA.findAll();

        Assert.assertEquals(0, databaseList.size());
    }

    @Test
    public void download3ObjTest() {
        amazonEntity.updateList();

        List<AmazonObjectEntity> databaseList = amazonObjectJPA.findAll();

        Assert.assertEquals(3, databaseList.size());
    }

    @Test
    public void downloadOneObjLessTest() {
        amazonEntity.updateList();

        amazonEntity.updateList2Elements();

        List<AmazonObjectEntity> databaseList = amazonObjectJPA.findAll();

        Assert.assertEquals(2, databaseList.size());
    }
}
