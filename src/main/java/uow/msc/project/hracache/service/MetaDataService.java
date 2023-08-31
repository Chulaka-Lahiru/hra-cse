package uow.msc.project.hracache.service;

import uow.msc.project.hracache.model.MetaDataEntity;

import java.util.List;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
public interface MetaDataService {

    double calculateHitRate() throws Exception;

    List<MetaDataEntity> getAPIMetaData(int limit) throws Exception;

    String saveMetaData(String requestType, String requestStatus, long dateTimeInst, double delay) throws Exception;

    List<List<String>> getLoadTestResults(String timePeriod) throws Exception;
}