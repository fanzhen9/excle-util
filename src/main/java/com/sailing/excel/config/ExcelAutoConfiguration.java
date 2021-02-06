package com.sailing.excel.config;

import com.sailing.excel.service.DownLoadService;
import com.sailing.excel.service.ExcelService;
import com.sailing.excel.service.ZipService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author fox
 */
@Configuration
@EnableConfigurationProperties(ExcelConfig.class)
public class ExcelAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = RestTemplate.class)
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
    @Bean
    public DownLoadService downLoadService(){
        return new DownLoadService();
    }

    @Bean
    public ExcelService excelService(RestTemplate restTemplate, ExcelConfig excelConfig, DownLoadService downLoadService){
        ExcelService excelService = new ExcelService(excelConfig,restTemplate,downLoadService);
        return excelService;
    }

    @Bean
    public ZipService zipService(ExcelConfig excelConfig){
        return new ZipService(excelConfig);
    }
}
