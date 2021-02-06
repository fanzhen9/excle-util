package com.sailing.excel.service;

import com.sailing.excel.annotation.Excel;
import com.sailing.excel.config.ExcelConfig;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author fox
 */
public class ExcelService {



    private ExcelConfig excelConfig;

    private RestTemplate restTemplate;

    private DownLoadService downLoadService;

    public ExcelService(ExcelConfig excelConfig,RestTemplate restTemplate,DownLoadService downLoadService){
        this.excelConfig = excelConfig;
        this.restTemplate = restTemplate;
        this.downLoadService = downLoadService;
    }
    public ExcelService(ExcelConfig excelConfig,RestTemplate restTemplate){
        this.excelConfig = excelConfig;
        this.restTemplate = restTemplate;
    }
    /**
     * 导入
     * @param list
     * @param clazz
     * @param id
     * @param <T>
     */
    public <T> void writeExcel(List<T> list,Class clazz,String id){
        //构建excel文档
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFCellStyle linkStyle = workbook.createCellStyle();
        XSSFFont cellFont = workbook.createFont();
        cellFont.setUnderline((byte) 1);
        cellFont.setColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
        linkStyle.setFont(cellFont);
        //构建sheet
        XSSFSheet createSheet = workbook.createSheet();
        //创建第一行
        XSSFRow row = createSheet.createRow(0);
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(Excel.class)){
                Excel annotation = field.getAnnotation(Excel.class);
                Integer index = annotation.index();
                String name = annotation.name();
                XSSFCell cell = row.createCell(index);
                cell.setCellValue(name);
            }
        }
        for (int i = 0; i < list.size(); i++) {
            XSSFRow dataRow = createSheet.createRow(i+1);
            Field[] dataFields = clazz.getDeclaredFields();
            T t = list.get(i);
            for (Field field : dataFields) {
                if(field.isAnnotationPresent(Excel.class)){
                    Excel annotation = field.getAnnotation(Excel.class);
                    Integer index = annotation.index();
                    boolean url = annotation.isUrl();
                    Class classType = annotation.fieldClassType();

                    XSSFCell cell = dataRow.createCell(index);
                    try {
                        String methodName = field.getName();
                        String str1 = methodName.substring(0, 1);
                        String str2 = methodName.substring(1);
                        String methodGet = "get" + str1.toUpperCase() + str2;
                        Method method = clazz.getMethod(methodGet);
                        Object object = method.invoke(t);
                        if(String.class == classType){
                            cell.setCellValue(String.valueOf(object));
                        }
                        if(Double.class == classType||Integer.class == classType){
                            cell.setCellValue(Double.valueOf(String.valueOf(object)));
                        }
                        if(Date.class == classType){
                            cell.setCellValue((Date)object);
                        }
                        if(Boolean.class == classType){
                            cell.setCellValue(Boolean.valueOf(String.valueOf(object)));
                        }
                        if(url && downLoadService != null){
                            File file = new File(excelConfig.getFilePath()+"/"+id+"/pic");
                            if(!file.exists()){
                                file.mkdirs();
                            }
                            String fileName = getSimpleId()+".jpg";
                            downLoadService.downLoadFile(excelConfig.getFilePath()+"/"+id+"/pic",fileName,restTemplate,String.valueOf(object));
                            //开始图片下载
                            CreationHelper createHelper = workbook.getCreationHelper();
                            XSSFHyperlink hyperlink = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.FILE);
                            hyperlink.setAddress("./pic/"+fileName);
                            cell.setHyperlink(hyperlink);
                            cell.setCellValue("【点击打开】");
                            cell.setCellStyle(linkStyle);
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }
        try {
            File file = new File(excelConfig.getFilePath()+"/"+id);
            if(!file.exists()){
                file.mkdirs();
            }
            FileOutputStream fileOut = new FileOutputStream(excelConfig.getFilePath()+"/"+id+"/"+id+".xlsx");
            workbook.write(fileOut);
            fileOut.close();
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    /**
     *
     * @param workbook
     * @param clazz
     * @param readline
     * @param <T>
     * @return
     */
    public <T> List<T> readExcel(XSSFWorkbook workbook,int sheetIndex,Class<T> clazz,int readline) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        XSSFSheet sheetAt = workbook.getSheetAt(sheetIndex);
        Integer rows = sheetAt.getLastRowNum();

        List<T> result = new ArrayList<T>();
        for (; readline <= rows; readline++) {
            XSSFRow row = sheetAt.getRow(readline);
            T t = clazz.newInstance();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if(field.isAnnotationPresent(Excel.class)){
                    Excel annotation = field.getAnnotation(Excel.class);
                    int columnIndex = annotation.index();
                    Class aClass = annotation.fieldClassType();
                    String methodName = field.getName();
                    String str1 = methodName.substring(0, 1);
                    String str2 = methodName.substring(1);
                    String methodSet = "set" + str1.toUpperCase() + str2;
                    Method method = clazz.getMethod(methodSet,aClass);
                    XSSFCell cell = row.getCell(columnIndex);
                    if(String.class == aClass && cell != null){
                        String value = cell.getStringCellValue();
                        method.invoke(t,value);
                    }
                    if((Integer.class == aClass||Double.class == aClass||Float.class == aClass) && cell != null){
                        double value = cell.getNumericCellValue();
                        method.invoke(t,aClass.cast(value));
                    }
                    if(Boolean.class == aClass && cell != null){
                        boolean value = cell.getBooleanCellValue();
                        method.invoke(t,aClass.cast(value));
                    }
                    if(Date.class == aClass && cell != null){
                        Date value = cell.getDateCellValue();
                        method.invoke(t,aClass.cast(value));
                    }
                }
            }
            result.add(t);
        }
        return result;
    }

    private String getSimpleId(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
