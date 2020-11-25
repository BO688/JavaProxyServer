package Server;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PublicServer {
    private ServerSocket serverSocket;
    public  MyServerClient myServerClient;
    private void CreatePublic(int Port) throws IOException {
        System.out.println("服务器启动成功.........");
        //监听服务器
        serverSocket=new ServerSocket(Port);
        while (true){
            Socket socket=serverSocket.accept();
            System.out.println("请求！");
            new MyServer(socket).run();
        }
    }
    private void CreateChannel(int Port) throws IOException {
        myServerClient=new MyServerClient(Port);
        System.out.println("隧道启动");
        myServerClient.KeepAlice();
    }
    class MyServer extends Thread {
        private Socket accept;
        MyServer(Socket accept){
            this.accept=accept;
        }
        @Override
        public void run() {
            try {
                InputStream inputStream = accept.getInputStream();
                String RequestMessage="";
                accept.setSoTimeout(1000);
                try {
                    byte b[]=new byte[1024];
                    try {
                        inputStream.read(b);
                    }catch (Exception e){
                    }
                    RequestMessage=new String(b).split("\r\n")[0];
                    System.out.println(RequestMessage);
                    accept.getOutputStream().write("HTTP/1.1 200 OK  \r\n\r\n".getBytes());
                    if(!"".equals(RequestMessage)){
                            myServerClient.GetAndSend(RequestMessage.split(" ")[1],accept.getOutputStream());
                    }else{
                        System.out.println("空请求");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    accept.close();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("参数不足使用默认端口号和地址");
            System.out.println("SPort:"+ 8888+",CPort:"+8080);
            PublicServer server=new PublicServer();
            server.CreateChannel(8888);
            server.CreatePublic(8080);
        }else{
            PublicServer server=new PublicServer();
            server.CreateChannel(Integer.parseInt(args[0]));
            server.CreatePublic(Integer.parseInt(args[1]));
        }
    }
}
