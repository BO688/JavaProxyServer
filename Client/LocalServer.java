package Client;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class LocalServer {
    Socket socket;
//    static int SPort=8888;
    static int SPort=7456;
    static  int CPort=5555;
     static boolean check=false;
    static String ip="172.81.247.205";
//    static String ip="127.0.0.1";
    private boolean EndWithNoHtml(String param){
        return param.endsWith(".jpg")||param.endsWith(".png")||param.endsWith(".gif");
    }
    LocalServer(int sport,int cport,String ip) throws Exception {
        SPort=sport;
        CPort=cport;
        this.ip=ip; init();
    }
    LocalServer() throws Exception {
        init();
    }
    public void init() throws Exception {
        while (true){
            try {
                socket=new Socket(ip,SPort);
                break;
            }catch (ConnectException CE){
//                CE.printStackTrace();
                continue;
            }
        }
        System.out.println("隧道已连接");
        while (true){
            String param=new DataInputStream(socket.getInputStream()).readUTF();
            if(param.trim().equals("")){
//                System.out.println("keepalive");
                continue;
            }
            System.out.println("param"+param);
            if(EndWithNoHtml(param)){
                new SendImg(GetOneStream(param)).run();
                while (check){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                byte []c=GetOne(param);
                new SendData(c).run();
                while (check){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
    byte[] GetOne(String param) throws Exception {
        URL url=new URL("http://127.0.0.1:"+CPort+param);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6 * 1000);
        conn.setReadTimeout(10000);
        if (conn.getResponseCode() == 200) {
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuffer strBuffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                strBuffer.append(line+"\n");
            }
            return strBuffer.toString().getBytes();
        }
        return "".getBytes();
    }
    InputStream GetOneStream(String param) throws Exception {
        URL url=new URL("http://127.0.0.1:"+CPort+param);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6 * 1000);
        return conn.getInputStream();
    }
    class SendData extends Thread{
        byte[] c;
        SendData(byte[]c){
            this.c=c;
        }
        @Override
        public void run() {
            try {
                Socket s=new Socket(ip,SPort);
                check=true;
                s.getOutputStream().write(c);
                s.close();
                check=false;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    class SendImg extends Thread{
        InputStream is;
        SendImg(InputStream is){
            this.is=is;
        }
        @Override
        public void run() {
            try {
                Socket s=new Socket(ip,SPort);
                check=true;
                int len=is.read();
//                OutputStreamWriter osw=new OutputStreamWriter(s.getOutputStream(),"GBK");
                while (len!=-1){
                    s.getOutputStream().write(len);
                    len=is.read();
                }
                s.close();
                check=false;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static void main(String[] args) throws Exception {
        if(args.length!=3){
            System.out.println("参数不足使用默认端口号和地址");
            System.out.println("SPort:"+ SPort+",CPort:"+CPort+",ip:"+ip);
               while (true){
                   try {
                       new LocalServer();
                   }catch (Exception e){
                       System.out.println("隧道断开，重新连接");
                   }
               }

        }else{
            while (true){
                try {
                    new LocalServer(Integer.parseInt(args[0]),Integer.parseInt(args[1]),args[2]);
                }catch (Exception e){
                    System.out.println("隧道断开，重新连接");
                }
            }
        }


    }
}

