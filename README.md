# Excel 导出插件

插件采用springboot 2.2.2.Release包开发,仅仅支持spirngboot 工程

-----

## 生成Excel

使用方法

添加依赖到pom.xml

```xml
<dependency>
    <groupId>com.sailing</groupId>
    <artifactId>excel</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

添加后内部已经将service添加到ioc容器中

excle导出方法

```java
List<FaceVO> list = new ArrayList<>();
FaceVO faceVO = new FaceVO();
faceVO.setDeviceId("310118222552");
faceVO.setDeviceName("纱帽路");
faceVO.setLatitude("51.584566");
faceVO.setLongitude("121.25844");
faceVO.setPicUrl("http://172.20.25.174:8080/aaa.jpg");
faceVO.setTime("2021-01-01 00:00:00");
list.add(faceVO);
String id = UUID.randomUUID().toString();
service.writeExcel(list, FaceVO.class, id);
```

对象中需要再要导出的字段上添加@Excle注解

```java
@Excel(index = 0,name = "设备编号")
private String deviceId;
@Excel(index = 1,name = "设备名称")
private String deviceName;
```

index表示导出excel第几列，name表示表头，isUrl 表示是否是图片url 默认为false，设置true可以下载，fieldClassType是字段类型，默认java.lang.String

## 打包

```java
@Autowired
ZipService zipService;

try {
    String result = zipService.zip("d://excel/"+id);
    System.out.println(result);
} catch (Exception e) {
    e.printStackTrace();
}
```

参数是需要打包的文件夹路径

返回是打好Zip包的文件路径

## Excel 导入

excel导入成list 和 导出类似，需要填写E@Excel注解

实现方法

```java
@Autowired
ZipService zipService;

XSSFWorkbook wb = new XSSFWorkbook(fis);
List<PeopleVo> faceVOList = excelService.readExcel(wb, 0, PeopleVo.class, 2);
System.out.println(faceVOList);
```

第二次参数是sheet 编号，第三个参数是返回集合的类型，第四个是从第几行开始扫描
