package uow.msc.project.hracache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.web.servlet.WebMvcMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@SpringBootApplication(exclude = WebMvcMetricsAutoConfiguration.class)
public class HRACacheWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(uow.msc.project.hracache.HRACacheWebApplication.class, args);
    }

}
