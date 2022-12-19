package com.jflove.plugins.asmr;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tanjun
 * @date 2022/11/22 17:24
 */
public class AsmrGay {
    private static HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    private static final String name = "步非烟";
    private static final String storage = "C:\\Users\\Administrator\\Music\\asmr/%s/%s";
    private static final String fileNameMatching = "点心";

    public static void main(String[] args) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.asmr.pw/api/public/search"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(String.format("""
                        {"path":"/","keyword":"%s"}
                        """,name)))
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jo = JSONUtil.parseObj((String) response.body());
        JSONArray datas = jo.getJSONArray("data");

        Map<String,String> fileUrls = new HashMap<>();
        datas.forEach(v->{
            getFileUrl((JSONObject) v,fileUrls,null);
        });
        //下载服务地址池子
        List<String> downloadService = List.of(
                "https://asmr.pipix.xyz%s","https://www.asmr.pw/d%s");
        fileUrls.forEach((k,v)->{
            Path fp = Path.of(String.format(storage,name,v));
            try {
                if (Files.exists(fp) && Files.size(fp) > 2048) {
                    System.out.println(String.format("文件[%s]存在，跳过。",v));
                    return;
                }
            }catch (IOException e){}
            for (String ds : downloadService){
                String fileUrl = String.format(ds, k);
                if(download(fileUrl,fp)){
                    break;
                }
            }
        });
    }

    private static boolean download(String fileUrl,Path fp){
        System.out.println(String.format("正在下载:%s",fileUrl));
        HttpRequest fileDw = HttpRequest.newBuilder()
                .uri(URI.create(fileUrl))
                .timeout(Duration.ofMinutes(10))
                .GET()
                .build();
        HttpResponse.BodyHandler<Path> pathBodyHandler = HttpResponse.BodyHandlers
                .ofFile(fp);
        try {
            HttpResponse response = client.send(fileDw, pathBodyHandler);
            if(response.statusCode() == 200){
                return true;
            }else if(response.statusCode() == 404){
                return false;
            }else if(response.statusCode() == 302){
                return false;
            }else{
                return false;
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
        return false;
    }

    private static void getFileUrl(JSONObject data,Map<String,String> fileUrls,String p){
        try {
            String name = data.getStr("name");
            if(p != null){
                data.putOpt("path",p);
            }
            String path = String.format("%s/%s", data.getStr("path"), name);
            String pathUrl = urlToEscape(path);
            if (isDir(name)) {
                HttpRequest dir = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.asmr.pw/api/public/path"))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                String.format("""
                                        {"path":"%s","page_num":1,"page_size":1000}
                                        """, path)))
                        .build();
                HttpResponse dirResp = client.send(dir, HttpResponse.BodyHandlers.ofString());
                JSONObject dirJo = JSONUtil.parseObj((String) dirResp.body());
                JSONObject dirData = dirJo.getJSONObject("data");
                JSONArray dirFiles = dirData.getJSONArray("files");
                dirFiles.forEach(v2 -> {
                    getFileUrl((JSONObject) v2,fileUrls,path);
                });
            } else if (fileNameMatching == null || pathUrl.indexOf(fileNameMatching) != -1){
                System.out.println(String.format("添加下载地址:%s", pathUrl));
                fileUrls.put(pathUrl, name);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private static boolean isDir(String str){
        if (str.endsWith(".m4a") || str.endsWith(".mp3") || str.endsWith(".mp4")) {
            return false;
        }else if(str.endsWith(".jpg") || str.endsWith(".png")){
            return false;
        }else if(str.indexOf(".") != -1){//目录带.应该是文件
            return false;
        }else{
            return true;
        }
    }

    /***
     * 转义表见:https://wenku.baidu.com/view/10cb78c09889680203d8ce2f0066f5335a816728.html?_wkts_=1669114320758&bdQuery=java+url+%E6%8B%AC%E5%8F%B7%E8%BD%AC%E4%B9%89
     */
     private static String urlToEscape(String str) {
            return str.replaceAll("%","%25")
                    .replaceAll(" ","%20")
                    .replaceAll("\\\"","%22")
                    .replaceAll("#","%23")
                    .replaceAll("&","%26")
                    .replaceAll("\\(","%28")
                    .replaceAll("\\)","%29")
                    .replaceAll("\\+","%2B")
                    .replaceAll(",","%2C")
                    //.replaceAll("/","%2F")
                    .replaceAll(":","%3A")
                    .replaceAll(";","%3B")
                    .replaceAll("<","%3C")
                    .replaceAll("=","%3D")
                    .replaceAll(">","%3E")
                    .replaceAll("\\?","%3F")
                    .replaceAll("@","%40")
                    .replaceAll("\\\\","%5C")
                    .replaceAll("\\[","%5B")
                    .replaceAll("]","%5D")
                    .replaceAll("\\|","%7C")
                    .replaceAll("\\{","%7B")
                    .replaceAll("}","%7D")
                    .replaceAll("~","%7e");
        }
}


