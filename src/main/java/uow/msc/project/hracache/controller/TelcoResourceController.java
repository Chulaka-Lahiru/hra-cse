package uow.msc.project.hracache.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uow.msc.project.hracache.model.MetaDataEntity;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.MetaDataService;
import uow.msc.project.hracache.service.TelcoResourceService;
import uow.msc.project.hracache.service.util.RequestStatusType;
import uow.msc.project.hracache.service.util.RequestType;
import uow.msc.project.hracache.service.util.ResponseUtils;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@RestController
@RequestMapping("/telcoproductcatalog")
@Slf4j
public class TelcoResourceController {

    @Autowired
    TelcoResourceService telcoResourceService;

    @Autowired
    MetaDataService metaDataService;
    private long apiDelay = 0L;
    private long apiRequestReceivedTime;
    private String requestStatus;
    private String requestType;

    //Get all from database
    @GetMapping("/gettelcoresources")
    public ResponseEntity<Page<TelcoResourceEntity>> getAllTelcoResources(
            @RequestParam(value = "page", defaultValue = ResponseUtils.DEFAULT_PAGE_NUM) Integer page,
            @RequestParam(value = "size", defaultValue = ResponseUtils.DEFAULT_PAGE_SIZE) Integer size) {
        try {
            requestType = RequestType.RequestTypeList.GET.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            Page<TelcoResourceEntity> telcoResources = telcoResourceService.getAllTelcoResources(page, size);
            if (telcoResources.isEmpty()) {
                apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
                requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
                saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nGET ALL: READ TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return new ResponseEntity<>(telcoResources, HttpStatus.OK);
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get data from Cache AND database
    @GetMapping("/readthroughtelcoresource/{id}")
    public ResponseEntity<TelcoResourceEntity> getTelcoResourceById(@PathVariable("id") long id) {
        try {
            requestType = RequestType.RequestTypeList.GET.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntity = telcoResourceService.findTelcoResourceById(id);
            log.info("TelcoResourceEntity TelcoResourceController {}", telcoResourceEntity);
            if (Objects.nonNull(telcoResourceEntity)) {
                apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
                log.info("\nGET: READ-THROUGH-CACHE TIME: " + apiDelay + "ms\n");

                requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
                saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
                return new ResponseEntity<>(telcoResourceEntity, HttpStatus.OK);
            }
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Get all from cache
    @GetMapping("/getallcacherecords")
    public ResponseEntity<List<TelcoResourceEntity>> getAllCacheRecords() {
        try {
            List<TelcoResourceEntity> telcoResources = telcoResourceService.getAllCacheRecords();
            if (telcoResources.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            log.info("\nGET ALL: READ FROM REDIS: " + telcoResources + "\n");

            return new ResponseEntity<>(telcoResources, HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Create telco resource in database
    @PostMapping("/createtelcoresource")
    public ResponseEntity<TelcoResourceEntity> createTelcoResource(@RequestBody TelcoResourceEntity telcoResourceEntity) {
        try {
            requestType = RequestType.RequestTypeList.POST.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResource(telcoResourceEntity);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nPOST: WRITE-TO-DATABASE TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Create resource with Write-Through func.
    @PostMapping("/writethroughtelcoresource")
    public ResponseEntity<TelcoResourceEntity> createTelcoResourceWithWriteThrough(@RequestBody TelcoResourceEntity telcoResourceEntity) {
        try {
            requestType = RequestType.RequestTypeList.POST.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResourceToCacheAndDb(telcoResourceEntity);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nPOST: WRITE-THROUGH-CACHE TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Create resource with Write-Back func.
    @PostMapping("/writebacktelcoresource")
    public ResponseEntity<TelcoResourceEntity> createTelcoResourceWithWriteBack(
            @RequestParam(value = "buffer-size", defaultValue = ResponseUtils.DEFAULT_WRITE_BACK_BUFFER) String bufferSize,
            @RequestBody TelcoResourceEntity telcoResourceEntity) {
        try {
            requestType = RequestType.RequestTypeList.POST.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntitySaved = telcoResourceService.saveTelcoResourceToCacheAndAsyncDb(telcoResourceEntity, bufferSize);
            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                    .buildAndExpand(telcoResourceEntitySaved.getId()).toUri();
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nPOST: WRITE-BACK-CACHE TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return ResponseEntity.created(location).build();
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Update Telco resource
    @PutMapping("/updatetelcoresource/{id}")
    public ResponseEntity<TelcoResourceEntity> updateTelcoResource(@PathVariable("id") long id, @RequestBody TelcoResourceEntity telcoResourceEntity) {
        try {
            requestType = RequestType.RequestTypeList.PUT.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            TelcoResourceEntity telcoResourceEntityUpdated = telcoResourceService.updateTelcoResource(id, telcoResourceEntity);
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nPUT: DATABASE RECORD UPDATE TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return new ResponseEntity<>(telcoResourceEntityUpdated, HttpStatus.OK);
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    //Delete Telco resource
    @DeleteMapping("/deletetelcoresource/{id}")
    public ResponseEntity<HttpStatus> deleteTelcoResource(@PathVariable("id") long id) {
        try {
            requestType = RequestType.RequestTypeList.DELETE.name();
            apiRequestReceivedTime = System.currentTimeMillis();
            telcoResourceService.deleteTelcoResource(id);
            apiDelay = System.currentTimeMillis() - apiRequestReceivedTime;
            log.info("\nDELETE: DATABASE AND CACHE RECORD DELETE TIME: " + apiDelay + "ms\n");

            requestStatus = RequestStatusType.RequestStatusTypeList.SUCCESS.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, apiDelay);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex) {
            requestStatus = RequestStatusType.RequestStatusTypeList.FAILED.name();
            saveMetaData(requestType, requestStatus, apiRequestReceivedTime, 0.00);
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getmetadata")
    public ResponseEntity<List<MetaDataEntity>> getMetaData(
            @RequestParam(value = "record-limit", defaultValue = ResponseUtils.DEFAULT_METADATA_RECORD_FETCH_SIZE) String recordLimit) {
        try {
            List<MetaDataEntity> metaDataList = metaDataService.getAPIMetaData(Integer.parseInt(recordLimit));
            if (metaDataList.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(metaDataList, HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void saveMetaData(String requestType, String requestStatus, long dateTimeInst, double delay) {
        try {
            metaDataService.saveMetaData(requestType, requestStatus, dateTimeInst, delay);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    @GetMapping("/getloadtestresults")
    public ResponseEntity<List<List<String>>> getLoadTestResults(
            @RequestParam(value = "test-time", defaultValue = ResponseUtils.DEFAULT_LOAD_TEST_TIME_SEC) String timePeriod) {
        try {
            List<List<String>> loadTestResults = metaDataService.getLoadTestResults(timePeriod);
            return new ResponseEntity<>(loadTestResults, HttpStatus.OK);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}