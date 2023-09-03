package uow.msc.project.hracache.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import uow.msc.project.hracache.external.repository.MetaDataRepository;
import uow.msc.project.hracache.external.repository.TelcoResourceRepository;
import uow.msc.project.hracache.model.MetaDataEntity;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.MetaDataService;
import uow.msc.project.hracache.service.TelcoResourceService;
import uow.msc.project.hracache.service.config.CacheRefreshConfig;
import uow.msc.project.hracache.service.exception.ResourceNotFoundException;
import uow.msc.project.hracache.service.util.RequestStatusType;
import uow.msc.project.hracache.service.util.RequestType;
import uow.msc.project.hracache.service.util.ResponseUtils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class TelcoResourceServiceImpl implements TelcoResourceService, MetaDataService {

    private static final DecimalFormat df = new DecimalFormat("0.00");
    private final TelcoResourceRepository telcoResourceRepository;
    private final MetaDataRepository metaDataRepository;
    private final RedisTemplate<String, TelcoResourceEntity> redisTemplate;
    private final CacheRefreshConfig cacheRefreshConfig;
    private final List<TelcoResourceEntity> telcoResourceEntityList = new ArrayList<>();
    private final List<List<String>> listOfListLoadTestResults = new ArrayList<>();
    private long lastCacheWriteBackResourceId = 0;
    private long hitCount;
    private long requestCount;

    @Override
    public String refreshAhead() {
        cacheRefreshConfig.refreshCache();
        return "Data Prefetched Successfully";
    }

    @Override
    public TelcoResourceEntity findTelcoResourceById(Long id) throws Exception {
        // Cache key generation
        String key = "resource_" + id;
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));

        // Retrieval of data from Cache, in case of a Cache-hit
        if (hasKey) {
            final TelcoResourceEntity post = operations.get(key);
            assert post != null;
            //Update of Rating parameter
            if (post.getRating() > 1) post.setRating(post.getRating() - 1);

            log.info("TELCO-RESOURCE-SERVICE: FROM REDIS: key: " + key + " value: " + post);
            log.info("TelcoResourceServiceImpl.findTelcoResourceById() : cache post >> " + post);

            //Sync the updated resource across db and cache
            this.updateTelcoResource(id, post);
            //Parameter counts for Hit Rate calculation
            hitCount += 1;
            requestCount += 1;
            return post;
        }

        // Retrieval of data from database in case of a Cache-miss
        final Optional<TelcoResourceEntity> telcoResource = telcoResourceRepository.findById(id);
        if (telcoResource.isPresent()) {
            operations.set(key, telcoResource.get());
            log.info("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + telcoResource.get());
            log.info("TelcoResourceServiceImpl.findTelcoResourceById() : cache insert >> " + telcoResource.get());
            requestCount += 1;
            return telcoResource.get();
        } else {
            return null;
        }
    }

    @Override
    public Page<TelcoResourceEntity> getAllTelcoResources(Integer page, Integer size) throws Exception {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("id")));
        return telcoResourceRepository.findAll(pageable);
    }

    @Override
    public List<TelcoResourceEntity> getAllCacheRecords() throws Exception {

        List<TelcoResourceEntity> telcoResourceEntityList = new ArrayList<>();
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        redisTemplate.setDefaultSerializer(new StringRedisSerializer());
        Set<String> redisKeys = redisTemplate.keys("resource_*");
        assert redisKeys != null;

        if (!redisKeys.isEmpty()) {
            for (String key : redisKeys) {
                TelcoResourceEntity telcoResourceEntity = operations.get(key);
                telcoResourceEntityList.add(telcoResourceEntity);

                log.info("TELCO-RESOURCE-SERVICE: FROM REDIS: key: " + key + " value: " + telcoResourceEntity);
                log.info("TelcoResourceServiceImpl.getAllCacheRecords() : cache record >> " + telcoResourceEntity);
            }
            telcoResourceEntityList = telcoResourceEntityList.stream().sorted(Comparator.comparing(TelcoResourceEntity::getId,
                    Comparator.nullsFirst(Comparator.naturalOrder()))).collect(Collectors.toList());
            Collections.reverse(telcoResourceEntityList);
            return telcoResourceEntityList;
        } else {
            return new ArrayList<TelcoResourceEntity>();
        }
    }

    @Override
    public TelcoResourceEntity saveTelcoResource(TelcoResourceEntity telcoResourceEntity) throws Exception {

        if (!Objects.nonNull(telcoResourceEntity.getCreatedDate()) || telcoResourceEntity.getCreatedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setCreatedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }
        if (!Objects.nonNull(telcoResourceEntity.getModifiedDate()) || telcoResourceEntity.getModifiedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setModifiedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }
        List<TelcoResourceEntity> telcoResourceEntityListForId = telcoResourceRepository.findAll(Sort.by("id").ascending());
        if (telcoResourceEntityListForId.size() == 0) telcoResourceEntity.setId(1L);
        else
            telcoResourceEntity.setId(telcoResourceEntityListForId.get(telcoResourceEntityListForId.size() - 1).getId() + 1);

        return telcoResourceRepository.save(telcoResourceEntity);

    }

    @Override
    public TelcoResourceEntity saveTelcoResourceToCacheAndDb(TelcoResourceEntity telcoResourceEntity) throws Exception {

        if (!Objects.nonNull(telcoResourceEntity.getCreatedDate()) || telcoResourceEntity.getCreatedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setCreatedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }
        if (!Objects.nonNull(telcoResourceEntity.getModifiedDate()) || telcoResourceEntity.getModifiedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setModifiedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }
        List<TelcoResourceEntity> telcoResourceEntityListForId = telcoResourceRepository.findAll(Sort.by("id").ascending());
        if (telcoResourceEntityListForId.size() == 0) telcoResourceEntity.setId(1L);
        else
            telcoResourceEntity.setId(telcoResourceEntityListForId.get(telcoResourceEntityListForId.size() - 1).getId() + 1);

        TelcoResourceEntity dbTelcoResource = telcoResourceRepository.save(telcoResourceEntity);
        String key = "resource_" + dbTelcoResource.getId();
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if (!hasKey) {
            operations.set(key, dbTelcoResource);
            log.info("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + dbTelcoResource);
        }
        return dbTelcoResource;

    }

    @Override
    public TelcoResourceEntity saveTelcoResourceToCacheAndAsyncDb(TelcoResourceEntity telcoResourceEntity, String bufferSize) throws Exception {
        //CreatedDate parameter is generated here
        if (!Objects.nonNull(telcoResourceEntity.getCreatedDate()) || telcoResourceEntity.getCreatedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setCreatedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }//ModifiedDate parameter is generated here
        if (!Objects.nonNull(telcoResourceEntity.getModifiedDate()) || telcoResourceEntity.getModifiedDate().isEmpty()) {
            LocalDateTime localDateTime = LocalDateTime.now();
            telcoResourceEntity.setModifiedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        }//Fetching of the record id of last indexed record in database
        List<TelcoResourceEntity> telcoResourceEntityListForLastId = telcoResourceRepository.findAll(Sort.by("id").descending());
        List<TelcoResourceEntity> telcoResourceEntityListCached = this.getAllCacheRecords();
        if (telcoResourceEntityListForLastId.size() == 0 && telcoResourceEntityListCached.size() == 0) telcoResourceEntity.setId(1L);
        else if (telcoResourceEntityListForLastId.size() == 0 && telcoResourceEntityListCached.size() > 0) telcoResourceEntity.setId(telcoResourceEntityListCached.get(0).getId() + 1);
        else telcoResourceEntity.setId(Long.max(lastCacheWriteBackResourceId + 1, telcoResourceEntityListForLastId.get(0).getId() + 1));

        // Cache key generation
        String key = "resource_" + telcoResourceEntity.getId();

        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));

        // Data is written to Cache if unavailable
        if (!hasKey) {
            operations.set(key, telcoResourceEntity);
            lastCacheWriteBackResourceId = telcoResourceEntity.getId();
            log.info("TELCO-RESOURCE-SERVICE: TO REDIS: key: " + key + " value: " + telcoResourceEntity);
        }

        // Buffering cached data
        telcoResourceEntityList.add(telcoResourceEntity);

        // Data is written to database asynchronously when the default/user-defined buffer size is reached
        if (telcoResourceEntityList.size() == Integer.parseInt(bufferSize)) {
            telcoResourceRepository.saveAll(telcoResourceEntityList);
            telcoResourceEntityList.clear();
        }
        return telcoResourceEntity;
    }

    @Override
    public TelcoResourceEntity updateTelcoResource(Long id, TelcoResourceEntity telcoResourceEntity) throws Exception {

        //Cache Key generation
        final String key = "resource_" + id;
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));

        Optional<TelcoResourceEntity> telcoResourceEntityExisting;
        if (hasKey) telcoResourceEntityExisting = Optional.ofNullable(operations.get(key));
        else telcoResourceEntityExisting = telcoResourceRepository.findById(id);

        telcoResourceEntity.setId(id);
        telcoResourceEntityExisting.ifPresent(resourceEntity -> telcoResourceEntity.setCreatedDate(resourceEntity.getCreatedDate()));
        LocalDateTime localDateTime = LocalDateTime.now();
        telcoResourceEntity.setModifiedDate(localDateTime.toString().substring(0, 10) + " " + localDateTime.toString().substring(11, 19));
        telcoResourceRepository.save(telcoResourceEntity);

        if (hasKey) {
            operations.set(key, telcoResourceEntity);
            log.info("TELCO-RESOURCE-SERVICE: UPDATED TO REDIS: key: " + key + " value: " + telcoResourceEntity);
            log.info("TelcoResourceServiceImpl.updateTelcoResource() : cache update >> " + telcoResourceEntity);
        }
        return telcoResourceEntity;
    }

    @Override
    public String deleteTelcoResource(Long id) throws Exception {

        //Cache Key generation
        final String key = "resource_" + id;
        final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));
        if (hasKey) {
            redisTemplate.delete(key);
            log.info("TELCO-RESOURCE-SERVICE: DELETED FROM REDIS: key: " + key);
            log.info("TelcoResourceServiceImpl.deletePost() : cache delete ID >> " + id);
        }
        final Optional<TelcoResourceEntity> telcoResource = telcoResourceRepository.findById(id);
        if (telcoResource.isPresent()) {
            telcoResourceRepository.delete(telcoResource.get());
            return "Successfully Deleted";
        } else {
            return "Resource Not Found";
        }
    }

    @Override
    public double calculateHitRate() throws Exception {
        if (this.requestCount > 0) {
            df.setRoundingMode(RoundingMode.UP);
            return Double.parseDouble(df.format(100 * ((double) this.hitCount / this.requestCount)));
        } else return 0.00;
    }

    @Override
    public List<MetaDataEntity> getAPIMetaData(int limit) throws Exception {
        List<MetaDataEntity> metaDataEntityList = metaDataRepository.findAll(Sort.by("id").descending());

        int newLimit = Math.min(metaDataEntityList.size(), limit);
        return metaDataEntityList.subList(0, newLimit);
    }

    @Override
    public String saveMetaData(String requestType, String requestStatus, long dateTimeInst, double delay) throws Exception {
        MetaDataEntity metaDataEntity = new MetaDataEntity();

        metaDataEntity.setRequestType(requestType);
        metaDataEntity.setRequestStatus(requestStatus);

        ZoneId sriLankanTimeZone = ZoneId.of("Asia/Colombo");
        Instant instant = Instant.ofEpochMilli(dateTimeInst);
        metaDataEntity.setDateTime(instant.atZone(sriLankanTimeZone).toString().substring(0, 10) + " " + instant.atZone(sriLankanTimeZone).toString().substring(11, 19));

        metaDataEntity.setDelay(delay);

        List<MetaDataEntity> metaDataEntityListForId = metaDataRepository.findAll();
        if (metaDataEntityListForId.size() == 0) metaDataEntity.setId(1L);
        else metaDataEntity.setId(Long.parseLong(String.valueOf(metaDataEntityListForId.size() + 1)));

        metaDataEntity.setHitRate(calculateHitRate());

        metaDataRepository.save(metaDataEntity);
        return "Meta Data Successfully Saved";

    }

    @Override
    public List<List<String>> getLoadTestResults(String timePeriod) throws Exception {
        TelcoResourceEntity loadTestEntity = new TelcoResourceEntity();
        loadTestEntity.setTelecomProduct("Load Test Product");
        loadTestEntity.setCategory("Load Test Category");
        loadTestEntity.setPriority("Level 05");
        loadTestEntity.setRating(10);

        long requestCount = 0;
        List<Long> apiDelayList = new ArrayList<>();
        TelcoResourceEntity loadTestResponseEntity;

        long startTime = System.currentTimeMillis();
        long endTime = startTime + Integer.parseInt(timePeriod) * 1000L;

        while (System.currentTimeMillis() <= endTime) {

            long apiStartTime01 = System.currentTimeMillis();
            loadTestResponseEntity = this.saveTelcoResourceToCacheAndAsyncDb(loadTestEntity, ResponseUtils.DEFAULT_WRITE_BACK_BUFFER);
            this.saveMetaData(RequestType.RequestTypeList.POST.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), apiStartTime01, System.currentTimeMillis() - apiStartTime01);
            apiDelayList.add(System.currentTimeMillis() - apiStartTime01);
            requestCount += 5;

            long apiStartTime02 = System.currentTimeMillis();
            this.updateTelcoResource(Objects.requireNonNull(loadTestResponseEntity).getId(), loadTestEntity);
            this.saveMetaData(RequestType.RequestTypeList.PUT.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), apiStartTime02, System.currentTimeMillis() - apiStartTime02);
            apiDelayList.add(System.currentTimeMillis() - apiStartTime02);
            requestCount += 5;

            long apiStartTime03 = System.currentTimeMillis();
            this.getAllTelcoResources(Integer.parseInt(ResponseUtils.DEFAULT_PAGE_NUM), Integer.parseInt(ResponseUtils.DEFAULT_PAGE_SIZE));
            this.saveMetaData(RequestType.RequestTypeList.GET.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), apiStartTime03, System.currentTimeMillis() - apiStartTime03);
            apiDelayList.add(System.currentTimeMillis() - apiStartTime03);
            requestCount += 3;

            long apiStartTime04 = System.currentTimeMillis();
            this.findTelcoResourceById(loadTestResponseEntity.getId());
            this.saveMetaData(RequestType.RequestTypeList.GET.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), apiStartTime04, System.currentTimeMillis() - apiStartTime04);
            apiDelayList.add(System.currentTimeMillis() - apiStartTime04);
            requestCount += 7;

            long apiStartTime05 = System.currentTimeMillis();
            this.deleteTelcoResource(loadTestResponseEntity.getId());
            this.saveMetaData(RequestType.RequestTypeList.DELETE.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), apiStartTime05, System.currentTimeMillis() - apiStartTime05);
            apiDelayList.add(System.currentTimeMillis() - apiStartTime05);
            requestCount += 5;

        }
        double throughput = Math.round(((double) requestCount / Double.parseDouble(timePeriod)) * 100.0) / 100.0;
        OptionalDouble averageOptionalDelay = apiDelayList
                .stream()
                .mapToDouble(a -> a)
                .average();
        double averageDelay = averageOptionalDelay.isPresent() ? averageOptionalDelay.getAsDouble() : 0.00;

        listOfListLoadTestResults.add(new ArrayList<>(Arrays.asList(String.valueOf(throughput), String.valueOf(Math.round(averageDelay * 100.0) / 100.0))));
        return listOfListLoadTestResults;
    }

}