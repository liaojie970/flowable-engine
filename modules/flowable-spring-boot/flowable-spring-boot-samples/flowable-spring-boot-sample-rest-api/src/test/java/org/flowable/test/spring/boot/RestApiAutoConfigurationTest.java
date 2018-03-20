package org.flowable.test.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.flowable.rest.api.DataResponse;
import org.flowable.rest.service.api.repository.ProcessDefinitionResponse;
import org.flowable.spring.boot.FlowableTransactionAutoConfiguration;
import org.flowable.spring.boot.ProcessEngineAutoConfiguration;
import org.flowable.spring.boot.RestApiAutoConfiguration;
import org.flowable.spring.boot.SecurityAutoConfiguration;
import org.flowable.spring.boot.idm.IdmEngineAutoConfiguration;
import org.flowable.spring.boot.idm.IdmEngineServicesAutoConfiguration;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author Josh Long
 */
public class RestApiAutoConfigurationTest {

    @Configuration
    @Import({ EmbeddedServletContainerAutoConfiguration.class,
            MultipartAutoConfiguration.class,
            ServerPropertiesAutoConfiguration.class,
            DataSourceAutoConfiguration.class,
            FlowableTransactionAutoConfiguration.class,
            IdmEngineAutoConfiguration.class,
            ProcessEngineAutoConfiguration.class,
            IdmEngineServicesAutoConfiguration.class,
            SecurityAutoConfiguration.class,
            RestApiAutoConfiguration.class,
            JacksonAutoConfiguration.class
    })
    protected static class BaseConfiguration {

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public ServerProperties serverProperties() {
            ServerProperties properties = new ServerProperties();
            properties.setPort(0);
            return properties;
        }
    }

    /*
     * @Configuration
     * 
     * @Import({EmbeddedServletContainerAutoConfiguration.class, DispatcherServletAutoConfiguration.class, ServerPropertiesAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
     * WebMvcAutoConfiguration.class, DataSourceAutoConfiguration.class, DataSourceProcessEngineAutoConfiguration.DataSourceConfiguration.class, RestApiAutoConfiguration.class }) public static class
     * RestApiConfiguration {
     * 
     * @Bean public RestTemplate restTemplate() { return new RestTemplate(); } }
     */
    @After
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    private AnnotationConfigEmbeddedWebApplicationContext context;

    @Test
    public void testRestApiIntegration() throws Throwable {

        this.context = new AnnotationConfigEmbeddedWebApplicationContext();
        this.context.register(BaseConfiguration.class);
        this.context.refresh();

        RestTemplate restTemplate = this.context.getBean(RestTemplate.class);

        String authenticationChallenge = "http://localhost:" + this.context.getEmbeddedServletContainer().getPort() +
            "/process-api/repository/process-definitions";

        ResponseEntity<DataResponse<ProcessDefinitionResponse>> response = restTemplate
            .exchange(authenticationChallenge, HttpMethod.GET, null, new ParameterizedTypeReference<DataResponse<ProcessDefinitionResponse>>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        DataResponse<ProcessDefinitionResponse> definitionResponse = response.getBody();
        assertThat(definitionResponse.getData())
            .extracting(ProcessDefinitionResponse::getName)
            .as("Process definitions names")
            .containsExactly("DogeProcess");
        assertThat(definitionResponse.getTotal()).isEqualTo(1);
    }
}
