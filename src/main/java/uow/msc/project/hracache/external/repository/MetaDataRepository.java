package uow.msc.project.hracache.external.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uow.msc.project.hracache.model.MetaDataEntity;

import java.util.List;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Repository
public interface MetaDataRepository extends JpaRepository<MetaDataEntity, Long> {

    @Override
    List<MetaDataEntity> findAll();
}
