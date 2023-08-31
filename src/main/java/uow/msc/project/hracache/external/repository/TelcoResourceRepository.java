package uow.msc.project.hracache.external.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uow.msc.project.hracache.model.TelcoResourceEntity;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Repository
public interface TelcoResourceRepository extends JpaRepository<TelcoResourceEntity, Long> {

    @Override
    Page<TelcoResourceEntity> findAll(Pageable pageable);

}
