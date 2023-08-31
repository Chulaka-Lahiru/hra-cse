package uow.msc.project.hracache.external.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uow.msc.project.hracache.model.TelcoResourceEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@DataJpaTest
public class TelcoResourceRepositoryTest {

    @Autowired
    private TelcoResourceRepository telcoResourceRepository;
    private TelcoResourceEntity telcoResourceEntity;

    @BeforeEach
    void setUp() {
        telcoResourceEntity = new TelcoResourceEntity();
        telcoResourceEntity.setId(1L);
        telcoResourceEntity.setCreatedDate(LocalDateTime.now().toString());
        telcoResourceEntity.setModifiedDate(LocalDateTime.now().toString());
        telcoResourceEntity.setPriority("Level 01");
        telcoResourceEntity.setCategory("Test Category");
        telcoResourceEntity.setRating(10);
        telcoResourceEntity.setTelecomProduct("Test Product");
        telcoResourceRepository.save(telcoResourceEntity);
    }

    //Success scenario
    @Test
    public void findAll_TestSuccess() {
        List<TelcoResourceEntity> telcoResourceEntities = telcoResourceRepository.findAll();
        assertThat(telcoResourceEntities.get(0).getId().equals(telcoResourceEntity.getId()));
        assertThat(telcoResourceEntities.get(0).getTelecomProduct().equals(telcoResourceEntity.getTelecomProduct()));
    }

    //Failed scenario
    @Test
    public void findAll_ResponseSize_TestSuccess() {
        List<TelcoResourceEntity> telcoResourceEntities = telcoResourceRepository.findAll();
        assertThat(telcoResourceEntities.size() > 0);
    }

    @AfterEach
    void tearDown() {
        telcoResourceRepository.deleteAll();
    }

}
