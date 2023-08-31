package uow.msc.project.hracache.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.plugin.core.SimplePluginRegistry;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Chulaka Lahiru - 2019515 - W1762231
 */
@Configuration
@ComponentScan("uow.msc.project.hracache.controller")
public class SwaggerConfig {

    @Bean
    public LinkDiscoverers discoverers() {
        List<LinkDiscoverer> plugins = new ArrayList<>();
        plugins.add(new CollectionJsonLinkDiscoverer());
        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/error").negate())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        String description = "Spring-boot REST API with mysql and Redis caching";
        return new ApiInfoBuilder()
                .title("Spring-boot REST API with mysql and Redis caching")
                .description(description)
                .termsOfServiceUrl("")
                .license("UOW-IIT")
                .licenseUrl("")
                .version("1.0")
                .build();
    }

}
