package com.sailing.excel.config;

import com.sailing.excel.service.DownLoadService;
import com.sailing.excel.service.ExcelService;
import com.sailing.excel.service.ZipService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

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
    /*@Bean
    public DownLoadService downLoadService(){
        return new DownLoadService();
    }*/

    @Bean
    public ZipService zipService(ExcelConfig excelConfig){
        return new ZipService(excelConfig);
    }

    @Bean
    @ConditionalOnMissingBean(ThreadPoolExecutor.class)
    public ThreadPoolExecutor executorService(){
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue();
        return new ThreadPoolExecutor(10, 20,
                60L, TimeUnit.SECONDS,blockingQueue
                );
    }

    @Bean
    public ExcelService excelService(RestTemplate restTemplate, ExcelConfig excelConfig,ThreadPoolExecutor threadPoolExecutor){
        ExcelService excelService = new ExcelService(excelConfig,restTemplate,threadPoolExecutor);
        return excelService;
    }
}
