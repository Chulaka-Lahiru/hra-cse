package uow.msc.project.hracache.service;

import org.springframework.data.domain.Page;
import uow.msc.project.hracache.model.TelcoResourceEntity;

import java.util.List;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
public interface TelcoResourceService {

    String refreshAhead();

    TelcoResourceEntity findTelcoResourceById(Long id) throws Exception;

    Page<TelcoResourceEntity> getAllTelcoResources(Integer page, Integer size) throws Exception;

    List<TelcoResourceEntity> getAllCacheRecords() throws Exception;

    TelcoResourceEntity saveTelcoResource(TelcoResourceEntity telcoResourceEntity) throws Exception;

    TelcoResourceEntity saveTelcoResourceToCacheAndDb(TelcoResourceEntity telcoResourceEntity) throws Exception;

    TelcoResourceEntity saveTelcoResourceToCacheAndAsyncDb(TelcoResourceEntity telcoResourceEntity, String bufferSize) throws Exception;

    TelcoResourceEntity updateTelcoResource(Long id, TelcoResourceEntity telcoResourceEntity) throws Exception;

    String deleteTelcoResource(Long id) throws Exception;

}
