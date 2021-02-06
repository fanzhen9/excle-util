package com.sailing.excel.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fox
 */
@ConfigurationProperties(prefix = "excel")
public class ExcelConfig {

    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
