package com.jflove.plugins.dns;


import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.commons.net.telnet.TelnetClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;

/**
 * @author: tanjun
 * @date: 2023/7/21 9:50 AM
 * @desc: 通过dns网站获得一个域名的所有ip,本机尝试ping,得到可用的ip
 */
public class GetAvailableIp extends WebSocketClient {

    private static HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public GetAvailableIp(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) throws InterruptedException {
        super(serverUri, protocolDraft, httpHeaders);
        this.connectBlocking();
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        System.out.println("握手...");
        for (Iterator<String> it = shake.iterateHttpFields(); it.hasNext(); ) {
            String key = it.next();
            System.out.println(key + ":" + shake.getFieldValue(key));
        }
    }

    @Override
    public void onMessage(String paramString) {
        //System.out.println("接收到消息：" + paramString);
        JSONObject jo = JSONUtil.parseObj(paramString);
        JSONObject data = jo.getJSONObject("data");
        String taskId = data.getStr("TaskId");
        //&& !data.getBool("polluted")
        if(StringUtils.hasLength(taskId)){
            String ip = data.getStr("src_ip");
            System.out.println(ip);
            boolean is = telnet(ip,1443);
            if(is){
                System.out.println("可以使用:" + ip);
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Disconnected");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("异常" + e);

    }


    public static void main(String[] args) throws Throwable{

        Map<String, String> httpHeaders = new HashMap<>();
//        httpHeaders.put(":Authority","coding.tools");
//        httpHeaders.put(":Method","POST");
//        httpHeaders.put(":Path","/cn/nslookup");
//        httpHeaders.put(":Scheme","https");
//        httpHeaders.put("Accept","*/*");
//        httpHeaders.put("Accept-Encoding","gzip, deflate, br");
//        httpHeaders.put("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8");
//        httpHeaders.put("Content-Length","62");
//        httpHeaders.put("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
//        httpHeaders.put("Cookie","_ga_5BEW8XFBSR=GS1.1.1689919625.1.0.1689919625.60.0.0; _ga=GA1.1.1397048679.1689919626; ezds=ffid%3D1%2Cw%3D1920%2Ch%3D1200; ezohw=w%3D1920%2Ch%3D955; __qca=P0-230593636-1689919626448; ezux_ifep_273036=true; ezux_lpl_273036=1689919702695|3de26d93-54a4-4d38-618f-14c5d213bb5c|false; ezux_et_273036=268; ezux_tos_273036=629");
//        httpHeaders.put("Origin","https://coding.tools");
//        httpHeaders.put("Referer","https://coding.tools/cn/nslookup");
//        httpHeaders.put("Sec-Ch-Ua","\"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"114\", \"Google Chrome\";v=\"114\"");
//        httpHeaders.put("Sec-Ch-Ua-Mobile","?0");
//        httpHeaders.put("Sec-Ch-Ua-Platform","\"macOS\"");
//        httpHeaders.put("Sec-Fetch-Dest","empty");
//        httpHeaders.put("Sec-Fetch-Mode","cors");
//        httpHeaders.put("Sec-Fetch-Site","\"macOS\"");
//        httpHeaders.put("Sec-Ch-Ua-Platform","\"macOS\"");
//        httpHeaders.put("Sec-Ch-Ua-Platform","\"macOS\"");
//        httpHeaders.put("Sec-Ch-Ua-Platform","\"macOS\"");


//
//        Runnable r = new Runnable(){
//            @Override
//            public void run() {
//                gai.send("{\"type\":\"DNS-PING\",\"data\":{\"domain\":\"hk-hkg.prod.surfshark.com\"}}");
//            }
//        };
//        new Thread(r).start();
//        new CountDownLatch(1).await();


        List<File> a = new ArrayList<>();
        loadFile(Path.of("/Users/tanjun/Downloads/Surfshark_Config"),a);
        List<Map<String,String>> effective = new ArrayList<>();
        a.forEach(v->{
            try {
                FileReader fr = new FileReader(v);
                try (Scanner sc = new Scanner(fr)) {
                    while (sc.hasNextLine()) {  //按行读取字符串
                        String line = sc.nextLine();
                        if(line.startsWith("remote ")){
                            String [] ls = line.split(" ");
                            effective.add(MapUtil.builder("dk",ls[2]).put("ip",ls[1]).put("path",v.getPath()).build());
                            try{
                                HttpRequest fileDw = HttpRequest.newBuilder()
                                        .uri(URI.create("https://1.1.1.1/dns-query?name=hk-hkg.prod.surfshark.com&type=A"))
                                        .header("Accept","application/dns-json")
                                        .timeout(Duration.ofMinutes(10))
                                        .GET()
                                        .build();
                                HttpResponse response = client.send(fileDw, HttpResponse.BodyHandlers.ofString());
                                JSONObject ipjo = JSONUtil.parseObj(response.body());
                                ipjo.getJSONArray("Answer").forEach(an->{
                                    JSONObject ano = (JSONObject) an;
                                    String ip = ano.getStr("data");
                                    effective.add(MapUtil.builder("dk",ls[2]).put("ip",ip).put("path",v.getPath()).build());
                                });
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        //GetAvailableIp gai = new GetAvailableIp(new URI("wss://www.dnsce.com/connect?token=MTY4OTkzMTkzMSQyeSQxMCR3cThnM1puZTlNdGExUjV3MFhTZEllanNKM0RMRGdpR1hpZTh6enNUTFhwQUdURmhzYzBIdQ=="),new Draft_6455(),httpHeaders);

        effective.forEach(v->{
//            gai.send(String.format("""
//                    {"type":"DNS-PING","data":{"domain":"%s"}}
//                    """,v.get("ip")));
            System.out.println(JSONUtil.toJsonStr(v));
            if(telnet(v.get("ip"),Integer.parseInt(v.get("dk")))){
                System.out.println("可以使用:" + v.get("ip"));
            }
        });
    }


    private static boolean telnet(String ip,Integer dk){
        try {
            TelnetClient telnetClinet = new TelnetClient();
            telnetClinet.setConnectTimeout(500);
            telnetClinet.connect(ip, dk);
            telnetClinet.disconnect();
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 加载目录中的文件
     * @param directory
     * @param fileList
     */
    private static void loadFile(Path directory, List<File> fileList){
        File[] fs = directory.toFile().listFiles();
        for (File f:fs) {
            if(f.isHidden() || f.getName().startsWith(".") //忽略隐藏文件
                    || f.getName().equalsIgnoreCase("target") // 忽略编译输出目录
                    || f.getName().equalsIgnoreCase("overlays") // 忽略编译目录
            ){
                continue;
            }else if(f.isFile()){
                fileList.add(f);
            }else if(f.isDirectory()){
                loadFile(f.toPath(),fileList);
            }
        }
    }
}
