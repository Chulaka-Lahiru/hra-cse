package uow.msc.project.hracache.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uow.msc.project.hracache.model.MetaDataEntity;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.impl.TelcoResourceServiceImpl;
import uow.msc.project.hracache.service.util.RequestStatusType;
import uow.msc.project.hracache.service.util.RequestType;
import uow.msc.project.hracache.service.util.ResponseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@WebMvcTest(TelcoResourceController.class)
public class TelcoResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TelcoResourceServiceImpl telcoResourceService;
    private TelcoResourceEntity telcoResourceEntity1;
    private TelcoResourceEntity telcoResourceEntity2;
    private List<MetaDataEntity> metaDataEntityList;
    private List<List<String>> loadTestResults;
    private Page<TelcoResourceEntity> telcoResourceEntityPage;
    private List<TelcoResourceEntity> telcoResourceEntityList;

    @BeforeEach
    void setUp() throws Exception {
        telcoResourceEntity1 = new TelcoResourceEntity();
        telcoResourceEntity1.setId(1L);
        telcoResourceEntity1.setCreatedDate(LocalDateTime.now().toString());
        telcoResourceEntity1.setModifiedDate(LocalDateTime.now().toString());
        telcoResourceEntity1.setPriority("Level 01");
        telcoResourceEntity1.setCategory("Test Category 01");
        telcoResourceEntity1.setRating(10);
        telcoResourceEntity1.setTelecomProduct("Test Product 01");

        telcoResourceEntity2 = new TelcoResourceEntity();
        telcoResourceEntity2.setId(2L);
        telcoResourceEntity2.setCreatedDate(LocalDateTime.now().toString());
        telcoResourceEntity2.setModifiedDate(LocalDateTime.now().toString());
        telcoResourceEntity2.setPriority("Level 02");
        telcoResourceEntity2.setCategory("Test Category 02");
        telcoResourceEntity2.setRating(20);
        telcoResourceEntity2.setTelecomProduct("Test Product 02");

        telcoResourceEntityList = new ArrayList<>();
        telcoResourceEntityList.add(telcoResourceEntity1);
        telcoResourceEntityList.add(telcoResourceEntity2);
        telcoResourceEntityPage = new PageImpl<>(telcoResourceEntityList);

        MetaDataEntity metaDataEntity = new MetaDataEntity();
        metaDataEntity.setId(1L);
        metaDataEntity.setHitRate(75.00);
        metaDataEntity.setDelay(52.05);
        metaDataEntity.setDateTime(LocalDateTime.now().toString());
        metaDataEntity.setRequestType(RequestType.RequestTypeList.GET.name());
        metaDataEntity.setRequestStatus(RequestStatusType.RequestStatusTypeList.IN_PROGRESS.name());

        metaDataEntityList = new ArrayList<>();
        metaDataEntityList.add(metaDataEntity);

        loadTestResults = new ArrayList<>();
        loadTestResults.add(new ArrayList<>(Arrays.asList("24", "40.73")));
    }

    @Test
    public void getAllTelcoResources_TestSuccess() throws Exception {
        when(telcoResourceService.getAllTelcoResources(0, 2)).thenReturn(telcoResourceEntityPage);
        this.mockMvc.perform(get("/telcoproductcatalog/gettelcoresources?page=0&size=2")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void getTelcoResourceById_TestSuccess() throws Exception {
        when(telcoResourceService.findTelcoResourceById(1L)).thenReturn(telcoResourceEntity1);
        this.mockMvc.perform(get("/telcoproductcatalog/readthroughtelcoresource/1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void getAllCachedRecords_TestSuccess() throws Exception {
        when(telcoResourceService.getAllCacheRecords()).thenReturn(telcoResourceEntityList);
        this.mockMvc.perform(get("/telcoproductcatalog/getallcacherecords")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void createTelcoResource_TestSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(telcoResourceEntity1);

        when(telcoResourceService.saveTelcoResource(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        this.mockMvc.perform(post("/telcoproductcatalog/createtelcoresource").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andDo(print()).andExpect(status().isCreated());
    }

    @Test
    public void writeThroughTelcoResource_NonCachedRecordSaveTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(telcoResourceEntity1);

        when(telcoResourceService.saveTelcoResource(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        this.mockMvc.perform(post("/telcoproductcatalog/writethroughtelcoresource").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    public void writeBackTelcoResource_NonCachedRecordSaveTest() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(telcoResourceEntity1);

        when(telcoResourceService.saveTelcoResource(telcoResourceEntity1)).thenReturn(telcoResourceEntity1);
        this.mockMvc.perform(post("/telcoproductcatalog/writebacktelcoresource?buffer-size=3").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andDo(print()).andExpect(status().isInternalServerError());
    }

    @Test
    public void updateTelcoResource_TestSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        ObjectWriter ow = mapper.writer().withDefaultPrettyPrinter();
        String requestJson = ow.writeValueAsString(telcoResourceEntity2);

        when(telcoResourceService.updateTelcoResource(1L, telcoResourceEntity2)).thenReturn(telcoResourceEntity2);
        this.mockMvc.perform(put("/telcoproductcatalog/updatetelcoresource/2").contentType(MediaType.APPLICATION_JSON).content(requestJson)).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void deleteTelcoResource_TestSuccess() throws Exception {
        when(telcoResourceService.deleteTelcoResource(1L)).thenReturn("Delete Operation Tested Successfully");
        this.mockMvc.perform(delete("/telcoproductcatalog/deletetelcoresource/1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void getMetaData_TestSuccess() throws Exception {
        when(telcoResourceService.getAPIMetaData(1)).thenReturn(metaDataEntityList);
        this.mockMvc.perform(get("/telcoproductcatalog/getmetadata?record-limit=1")).andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void getLoadTestResults_TestSuccess() throws Exception {
        when(telcoResourceService.getLoadTestResults(ResponseUtils.DEFAULT_LOAD_TEST_TIME_SEC)).thenReturn(loadTestResults);
        this.mockMvc.perform(get("/telcoproductcatalog/getloadtestresults?test-time=1")).andDo(print()).andExpect(status().isOk());
    }

    @AfterEach
    void tearDown() throws Exception {
        telcoResourceService.deleteTelcoResource(1L);
        telcoResourceService.deleteTelcoResource(2L);
    }

}