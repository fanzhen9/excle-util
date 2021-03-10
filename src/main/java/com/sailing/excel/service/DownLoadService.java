package com.sailing.excel.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * @author fox
 */
public class DownLoadService implements Runnable{

    private String path;

    private String fileName;

    private RestTemplate restTemplate;

    private String url;

    private CountDownLatch countDownLatch;

    public DownLoadService(String path, String fileName, RestTemplate restTemplate, String url,CountDownLatch countDownLatch) {
        this.path = path;
        this.fileName = fileName;
        this.restTemplate = restTemplate;
        this.url = url;
        this.countDownLatch = countDownLatch;
    }

    public void run() {
        try {
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
            HttpStatus statusCode = responseEntity.getStatusCode();
            if(statusCode == HttpStatus.NOT_FOUND){
                return;
            }
            //获取entity中的数据
            byte[] body = responseEntity.getBody();
            //创建输出流  输出到本地
            FileOutputStream fileOutputStream = new FileOutputStream(new File(path + "/" + fileName));
            fileOutputStream.write(body);
            //关闭流
            fileOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            countDownLatch.countDown();
        }
    }
}
