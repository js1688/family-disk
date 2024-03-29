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

    private static final String name = "唐樱樱";
    private static final String storage = "/Users/tanjun/Music/local/asmr/%s/%s";
    private static final String fileNameMatching = "";

    public static void main(String[] args) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.asmrgay.com/api/fs/search"))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(String.format("""
                        {"parent":"/","keywords":"%s","page":1,"per_page":1000,"password":""}
                        """,name)))
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject jo = JSONUtil.parseObj((String) response.body());
        JSONArray datas = jo.getJSONObject("data").getJSONArray("content");

        Map<String,String> fileUrls = new HashMap<>();
        datas.forEach(v->{
            getFileUrl((JSONObject) v,fileUrls);
        });
        //下载服务地址池子
        List<String> downloadService = List.of(
                "https://asmr.pw/d%s?sign=%s","https://asmr.pipix.xyz%s?sign=%s","https://www.asmrgay.com/d%s?sign=%s");
        fileUrls.forEach((k,v)->{
            Path fp = Path.of(String.format(storage,name,v));
            try {
                if (Files.exists(fp) && Files.size(fp) > 2048) {
                    System.out.println(String.format("文件[%s]存在，跳过。",v));
                    return;
                }
            }catch (IOException e){}
            //获取签名
            String sign = getSign(k);
            for (String ds : downloadService){
                String fileUrl = String.format(ds, urlToEscape(k),sign);
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

    private static String getSign(String path){
        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://asmr.pw/api/fs/get"))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(String.format("""
                        {"password":"","path":"%s"}
                        """,path)))
                    .build();
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONObject jo = JSONUtil.parseObj((String) response.body());
            JSONObject data = jo.getJSONObject("data");
            return data.getStr("sign");
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private static void getFileUrl(JSONObject data,Map<String,String> fileUrls){
        try {
            String name = data.getStr("name");
            String path = String.format("%s/%s", data.getStr("parent"), name);
            if(data.getBool("is_dir")){
                HttpRequest dir = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.asmrgay.com/api/fs/list"))
                        .timeout(Duration.ofMinutes(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                String.format("""
                                        {"path":"%s","password":"","page":1,"per_page":1000,"refresh":false}
                                        """, path)))
                        .build();
                HttpResponse dirResp = client.send(dir, HttpResponse.BodyHandlers.ofString());
                JSONObject dirJo = JSONUtil.parseObj((String) dirResp.body());
                JSONObject dirData = dirJo.getJSONObject("data");
                JSONArray dirFiles = dirData.getJSONArray("content");
                dirFiles.forEach(v2 -> {
                    ((JSONObject) v2).putOpt("parent",path);
                    getFileUrl((JSONObject) v2,fileUrls);
                });
            }else{
                //过滤文件
                if(fileNameMatching != null && fileNameMatching.length() != 0 && name.indexOf(fileNameMatching) == -1){
                    return;
                }
                System.out.println(String.format("添加下载地址:%s", path));
                fileUrls.put(path, name);
            }
        }catch (Throwable e){
            e.printStackTrace();
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


