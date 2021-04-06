package com.sailing.excel.service;

import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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

    private String cookIe;

    public DownLoadService(String path, String fileName, RestTemplate restTemplate, String url,CountDownLatch countDownLatch,String cookIe) {
        this.path = path;
        this.fileName = fileName;
        this.restTemplate = restTemplate;
        this.url = url;
        this.countDownLatch = countDownLatch;
        this.cookIe = cookIe;
    }

    public void run() {
        try {
            URI uri = new URI(url);
            HttpEntity entity = null;
            if(!StringUtils.isEmpty(cookIe)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("cookie",cookIe);
                entity = new HttpEntity(headers);
            }
            ResponseEntity<byte[]> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            HttpStatus statusCode = responseEntity.getStatusCode();
            if(statusCode == HttpStatus.NOT_FOUND){
                return;
            }
            //获取entity中的数据
            byte[] body = responseEntity.getBody();
            //判断是否是base64字符串
            String base64Str = new String(body).replaceAll("\"","");
            String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
            Boolean isLegal = base64Str.matches(base64Pattern);
            //如果是图片的base64
            if(isLegal){
                generateImage(base64Str,path + "/" + fileName);
                return;
            }
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

    /**
     * base64转图片
     * @param base64str base64码
     * @param savePath 图片路径
     * @return
     */
    public  boolean generateImage(String base64str, String savePath) {
        //对字节数组字符串进行Base64解码并生成图片
        if (base64str == null) {
            return false;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            //Base64解码
            byte[] b = decoder.decodeBuffer(base64str);
            for (int i = 0; i < b.length; ++i) {
                //调整异常数据
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            //生成jpeg图片
            OutputStream out = new FileOutputStream(savePath);
            out.write(b);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
