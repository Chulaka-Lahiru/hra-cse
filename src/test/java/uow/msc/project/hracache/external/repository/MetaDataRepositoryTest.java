package uow.msc.project.hracache.external.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uow.msc.project.hracache.model.MetaDataEntity;
import uow.msc.project.hracache.service.util.RequestStatusType;
import uow.msc.project.hracache.service.util.RequestType;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@DataJpaTest
public class MetaDataRepositoryTest {

    @Autowired
    private MetaDataRepository metaDataRepository;
    private MetaDataEntity metaDataEntity;

    @BeforeEach
    void setUp() {
        metaDataEntity = new MetaDataEntity();
        metaDataEntity.setId(1L);
        metaDataEntity.setHitRate(75.00);
        metaDataEntity.setDelay(52.05);
        metaDataEntity.setDateTime(LocalDateTime.now().toString());
        metaDataEntity.setRequestType(RequestType.RequestTypeList.GET.name());
        metaDataEntity.setRequestStatus(RequestStatusType.RequestStatusTypeList.IN_PROGRESS.name());
        metaDataRepository.save(metaDataEntity);
    }

    //Success scenario
    @Test
    public void findAll_MetaData_TestSuccess() {
        List<MetaDataEntity> metaDataEntities = metaDataRepository.findAll();
        assertThat(metaDataEntities.get(0).getId().equals(metaDataEntity.getId()));
        assertThat(String.valueOf(metaDataEntities.get(0).getDelay()).equals(String.valueOf(metaDataEntity.getDelay())));
    }

    //Failed scenario
    @Test
    public void findAll_MetaData_ResponseSize_TestSuccess() {
        List<MetaDataEntity> metaDataEntities = metaDataRepository.findAll();
        assertThat(metaDataEntities.size() > 0);
    }

    @AfterEach
    void tearDown() {
        metaDataRepository.deleteAll();
    }

}
