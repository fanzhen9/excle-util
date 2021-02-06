package com.sailing.excel.service;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author fox
 */
public class DownLoadService {

    @Async
    public void downLoadFile(String path, String fileName, RestTemplate restTemplate, String url) throws IOException {
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);
        //获取entity中的数据
        byte[] body = responseEntity.getBody();
        //创建输出流  输出到本地
        FileOutputStream fileOutputStream = new FileOutputStream(new File(path+"/"+fileName));
        fileOutputStream.write(body);
        //关闭流
        fileOutputStream.close();
    }
}
