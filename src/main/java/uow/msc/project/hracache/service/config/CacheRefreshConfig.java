package uow.msc.project.hracache.service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uow.msc.project.hracache.external.repository.TelcoResourceRepository;
import uow.msc.project.hracache.model.TelcoResourceEntity;
import uow.msc.project.hracache.service.util.ResponseUtils;

import java.util.List;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Configuration
@Slf4j
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class CacheRefreshConfig {

    @Autowired
    private TelcoResourceRepository telcoResourceRepository;

    @Autowired
    private RedisTemplate<String, TelcoResourceEntity> redisTemplate;

    @Scheduled(initialDelay = ResponseUtils.CACHE_REFRESH_INITIAL_DELAY, fixedRate = ResponseUtils.CACHE_REFRESH_RATE)
    @Async
    public void refreshCache() {
        final ValueOperations<String, TelcoResourceEntity> operations = redisTemplate.opsForValue();
        List<TelcoResourceEntity> telcoResourceEntities = telcoResourceRepository.findAll();

        // Cache is loaded with prefetched data from database with reference to Rating (<5)
        for (TelcoResourceEntity telcoResourceEntity : telcoResourceEntities) {
            //Cache key generation
            String key = "resource_" + telcoResourceEntity.getId();
            final boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key));
            //Condition check for record filtering
            if (!hasKey && telcoResourceEntity.getRating() < ResponseUtils.CACHE_REFRESH_RATING_PARAM_THRESHOLD) {
                operations.set(key, telcoResourceEntity);
                log.info("TELCO-RESOURCE-SERVICE: DATA REFRESH TO REDIS: key: " + key + " value: " + telcoResourceEntity);
            }
        }
    }

}

