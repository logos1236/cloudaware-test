package ru.armishev;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.armishev.entity.AmazonObjectEntity;
import ru.armishev.jpa.AmazonObjectJPA;
import ru.armishev.service.AmazonEntityTestMock;

import java.util.*;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class AmazonJPATest {
    private final AmazonObjectJPA amazonObjectJPA;
    private final AmazonEntityTestMock amazonEntity;

    @Autowired
    public AmazonJPATest(AmazonObjectJPA amazonObjectJPA) {
        this.amazonObjectJPA = amazonObjectJPA;
        this.amazonEntity = new AmazonEntityTestMock(amazonObjectJPA);
    }

    @Test
    public void downloadS3ObjTest() {
        amazonEntity.updateList();

        List<AmazonObjectEntity> databaseList = amazonObjectJPA.findAll();

        Assert.assertEquals(3, databaseList.size());
    }
}
