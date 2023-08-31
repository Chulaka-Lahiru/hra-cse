package uow.msc.project.hracache.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import uow.msc.project.hracache.external.repository.MetaDataRepository;
import uow.msc.project.hracache.external.repository.TelcoResourceRepository;
import uow.msc.project.hracache.model.MetaDataEntity;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.config.CacheRefreshConfig;
import uow.msc.project.hracache.service.exception.ResourceNotFoundException;
import uow.msc.project.hracache.service.util.RequestStatusType;
import uow.msc.project.hracache.service.util.RequestType;
import uow.msc.project.hracache.service.util.ResponseUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
public class TelcoResourceServiceImplTest {

    @Mock
    private TelcoResourceRepository telcoResourceRepository;
    @Mock
    private MetaDataRepository metaDataRepository;
    @Mock
    private RedisTemplate<String, TelcoResourceEntity> redisTemplate;
    @Mock
    private CacheRefreshConfig cacheRefreshConfig;
    @Mock
    private ValueOperations<String, TelcoResourceEntity> valueOperations;
    private TelcoResourceServiceImpl telcoResourceServiceImpl;
    private AutoCloseable autoCloseable;
    private TelcoResourceEntity telcoResourceEntity1, telcoResourceEntity2;
    private List<TelcoResourceEntity> telcoResourceEntityList;
    private MetaDataEntity metaDataEntity;
    private List<MetaDataEntity> metaDataEntityList;
    private Pageable pageable;
    private Page<TelcoResourceEntity> pageTelco;

    @BeforeEach
    void setUp() throws Exception {
        autoCloseable = MockitoAnnotations.openMocks(this);
        telcoResourceServiceImpl = new TelcoResourceServiceImpl(telcoResourceRepository, metaDataRepository, redisTemplate, cacheRefreshConfig);

        telcoResourceEntity1 = new TelcoResourceEntity();
        telcoResourceEntity1.setId(1L);
        telcoResourceEntity1.setPriority("Level 01");
        telcoResourceEntity1.setCategory("Test Category");
        telcoResourceEntity1.setRating(4);
        telcoResourceEntity1.setTelecomProduct("Test Product");

        telcoResourceEntity2 = new TelcoResourceEntity();
        telcoResourceEntity2.setId(2L);
        telcoResourceEntity2.setPriority("Level 02");
        telcoResourceEntity2.setCategory("Test Category");
        telcoResourceEntity2.setRating(20);
        telcoResourceEntity2.setTelecomProduct("Test Product");

        pageable = PageRequest.of(0, 1, Sort.by(Sort.Order.desc("id")));

        telcoResourceEntityList = new ArrayList<>();
        telcoResourceEntityList.add(telcoResourceEntity1);
        pageTelco = new PageImpl<>(telcoResourceEntityList);

        metaDataEntity = new MetaDataEntity();
        metaDataEntity.setId(null);
        metaDataEntity.setRequestType("");
        metaDataEntity.setRequestStatus("");
        metaDataEntity.setDelay(0.00);
        metaDataEntity.setHitRate(0.00);
        metaDataEntity.setDateTime(String.valueOf(LocalDateTime.now()));

        metaDataEntityList = new ArrayList<>();
        metaDataEntityList.add(metaDataEntity);

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void refreshAheadTest() {
        when(telcoResourceRepository.findAll()).thenReturn(telcoResourceEntityList);
        assertThat(telcoResourceServiceImpl.refreshAhead()).isEqualTo("Data Prefetched Successfully");
    }

    @Test
    public void findTelcoResourceById_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceRepository.findById(1L)).thenReturn(Optional.ofNullable(telcoResourceEntity1));
        assertEquals(telcoResourceServiceImpl.findTelcoResourceById(1L).getTelecomProduct(), telcoResourceEntity1.getTelecomProduct());

        when(Boolean.TRUE.equals(redisTemplate.hasKey("resource_2"))).thenReturn(true);
        when(valueOperations.get("resource_2")).thenReturn(telcoResourceEntity2);
        assertEquals(telcoResourceServiceImpl.findTelcoResourceById(2L).getTelecomProduct(), telcoResourceEntity2.getTelecomProduct());
    }

    @Test
    public void getAllTelcoResources_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceRepository.findAll(pageable)).thenReturn(pageTelco);
        assertThat(telcoResourceServiceImpl.getAllTelcoResources(Integer.parseInt(ResponseUtils.DEFAULT_PAGE_NUM), 1).toList().get(0).getCategory()).isEqualTo(telcoResourceEntity1.getCategory());
    }

    @Test
    public void getAllCachedRecords_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        Set<String> stringSet = new HashSet<>();
        stringSet.add("resource_2");
        when(redisTemplate.keys("resource_*")).thenReturn(stringSet);
        when(valueOperations.get("resource_2")).thenReturn(telcoResourceEntity2);
        assertThat(telcoResourceServiceImpl.getAllCacheRecords().get(0).getCategory()).isEqualTo(telcoResourceEntity2.getCategory());
    }

    @Test
    public void createTelcoResource_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceRepository.save(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        assertThat(telcoResourceServiceImpl.saveTelcoResource(telcoResourceEntity1)).isEqualTo(telcoResourceEntity1);
    }

    @Test
    public void saveTelcoResourceToCacheAndDb_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceRepository.save(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        assertThat(telcoResourceServiceImpl.saveTelcoResourceToCacheAndDb(telcoResourceEntity1)).isEqualTo(telcoResourceEntity1);
    }

    @Test
    public void saveTelcoResourceToCacheAndAsyncDb_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceRepository.save(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        assertThat(telcoResourceServiceImpl.saveTelcoResourceToCacheAndAsyncDb(telcoResourceEntity1, "3")).isEqualTo(telcoResourceEntity1);
    }

    @Test
    public void updateTelcoResource_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        TelcoResourceEntity telcoResourceEntity1_updated = telcoResourceEntity1;
        telcoResourceEntity1_updated.setTelecomProduct("Test Product-updated");
        when(telcoResourceRepository.save(telcoResourceEntity1_updated)).thenReturn(telcoResourceEntity1_updated);
        assertThat(telcoResourceServiceImpl.updateTelcoResource(1L, telcoResourceEntity1_updated)).isEqualTo(telcoResourceEntity1_updated);
    }

    @Test
    public void deleteTelcoResource_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class, Mockito.CALLS_REAL_METHODS);

        when(Boolean.TRUE.equals(redisTemplate.hasKey("resource_1"))).thenReturn(true);
        when(telcoResourceRepository.findById(1L)).thenReturn(Optional.ofNullable(telcoResourceEntity1));
        assertEquals(telcoResourceServiceImpl.deleteTelcoResource(1L), "Successfully Deleted");
    }

    @Test
    public void deleteTelcoResource_ResourceNotFoundTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class, Mockito.CALLS_REAL_METHODS);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> telcoResourceServiceImpl.deleteTelcoResource(1L)).withMessage(null);
    }

    @Test
    public void calculateHitRate_ZeroReturnTest() throws Exception {
        assertThat(telcoResourceServiceImpl.calculateHitRate()).isEqualTo(0.00);
    }

    @Test
    public void getAPIMetaData_SuccessTest() throws Exception {
        mock(MetaDataEntity.class);
        mock(MetaDataRepository.class);

        assertNotEquals(telcoResourceServiceImpl.getAPIMetaData(1), metaDataEntityList);
    }

    @Test
    public void saveMetaData_SuccessTest() throws Exception {
        mock(MetaDataEntity.class);
        mock(MetaDataRepository.class);

        when(metaDataRepository.save(metaDataEntity)).thenReturn(metaDataEntity);
        assertThat(telcoResourceServiceImpl.saveMetaData(RequestType.RequestTypeList.GET.name(), RequestStatusType.RequestStatusTypeList.SUCCESS.name(), System.currentTimeMillis(), 50.86)).isEqualTo("Meta Data Successfully Saved");
    }

    @Test
    public void getLoadTestResults_givenTimePeriod_SuccessTest() throws Exception {
        mock(TelcoResourceEntity.class);
        mock(TelcoResourceRepository.class);

        when(telcoResourceServiceImpl.getAllTelcoResources(1, 1)).thenReturn(pageTelco);
        when(Boolean.TRUE.equals(redisTemplate.hasKey("resource_1"))).thenReturn(true);
        when(valueOperations.get("resource_1")).thenReturn(telcoResourceEntity1);
        when(telcoResourceRepository.findById(1L)).thenReturn(Optional.ofNullable(telcoResourceEntity1));

        assertNotEquals(telcoResourceServiceImpl.getLoadTestResults("1"), new ArrayList<>(new ArrayList<String>(Arrays.asList("15.55", "25.00"))));
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

}