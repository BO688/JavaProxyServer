package NIO;

import NIO.pojo.MyRequest;
import NIO.pojo.MyResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class NIOLocalServer {
    ChannelFuture f1;
    Channel c;
    ChannelFuture f;
    int CPort=5555;
    String host="127.0.0.1";
    NIOLocalServer(int port){
    EventLoopGroup elp=new NioEventLoopGroup();
    EventLoopGroup elp1=new NioEventLoopGroup();
        try {
        Bootstrap b=new Bootstrap();
        Bootstrap b1=new Bootstrap();
        b1.group(elp1).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new StringEncoder());
                ch.pipeline().addLast(new StringDecoder());
                ch.pipeline().addLast("serverHandler", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    }
                    @Override
                    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                    }
                    @Override
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
                        ctx.close();
                        cause.printStackTrace();
                        while (true){
                            try {
                                f1=b1.connect(host,6666).sync();
                                c=f1.channel();
                                break;
                            }catch (Exception e){
                            }
                        }

                    }
                });
            }
        });
            while (true){
                try {
                    f1=b1.connect(host,6666).sync();
                    c=f1.channel();
                    break;
                }catch (Exception e){
                }
            }
        System.out.println("NIOLoopRequest连接成功，端口：6666" );
        b.group(elp)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(
                                new ObjectDecoder(1024 *1024*1024, ClassResolvers
                                        .cacheDisabled(getClass().getClassLoader())));
                        ch.pipeline().addLast(new ObjectEncoder());
                        ch.pipeline().addLast("serverHandler", new ChannelInboundHandlerAdapter() {

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String param=((MyRequest) msg).getParam();
                                String ID=((MyRequest) msg).getRequestId();
                                URL url=new URL("http://127.0.0.1:"+CPort+param.replace("@#$%",""));
                                System.out.println(url.getPath());
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setConnectTimeout(6 * 1000);
                                MyResponse myResponse=new MyResponse();
                                myResponse.setParam(param);
                                byte[] b;
                                try {
                                    System.out.println(conn.getResponseCode());
                                    if(conn.getResponseCode()==200){
                                        b=readInputStream(conn.getInputStream(),param);
                                    }else{
                                        b=readInputStream(conn.getErrorStream(),param);
                                    }
                                    myResponse.setCode(conn.getResponseCode());
                                }catch (Exception e){

                                    b="404 not found".getBytes();
                                    myResponse.setCode(404);
                                    System.out.println(new String (b));
                                }
                                myResponse.setResponseId(ID);
                                myResponse.setContent(b);
//                                System.out.println(new String(b,"UTF-8"));
//                                System.out.println(new String(b,"ISO8859-1"));
                                ctx.writeAndFlush(myResponse);
                            }

                            @Override
                            public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
                                ctx.close();
                                cause.printStackTrace();
                            }
                        });
                    }
                });
        // 绑定端口，开始接收进来的连接
            while (true){
                try {
                    f= b.connect(host,port).sync();
                    System.out.println("LocalServer连接成功，端口：" + port);
                    f.channel().closeFuture().sync();
                }catch (Exception e){
                }
            }
    }  finally {
        // 优雅的关闭
        elp.shutdownGracefully();
    }
}
    public static byte[] readInputStream(InputStream inStream,String param) throws Exception{
        if(param.endsWith(".png")||param.endsWith(".jpg")||param.endsWith(".jpeg")||param.endsWith(".gif")){
//            String str= getStrFromInsByCode(inStream,"ISO8859-1");
            byte[]b=MyGetStrFromInsByChar(inStream);
            FileOutputStream fos1=new FileOutputStream("ISO8859-1test.jpg");
            fos1.write(b);
//            System.out.println(new String(b));
            fos1.close();
            return b;
        }else{
            String str= getStrFromInsByCode(inStream,"UTF-8");
//        System.out.println(str);
            return str.getBytes();
        }

    }
    public static String getStrFromInsByCode(InputStream is, String code){
        StringBuilder builder=new StringBuilder();
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new InputStreamReader(is,code));
            String line;
            while((line=reader.readLine())!=null){
                builder.append(line+"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
    public static byte[] MyGetStrFromInsByChar(InputStream is) throws IOException {
        List<Character>list=new LinkedList<>();
        try {
            int len;
         while ((len=is.read())!=-1){
             list.add((char)len);
         }
           Object[]objects= list.toArray();
         char[]c=new char[objects.length];
            for (int i = 0; i <objects.length ; i++) {
                c[i]=(char)objects[i];
            }
            Charset cs = Charset.forName("ISO8859-1");
            CharBuffer cb = CharBuffer.allocate(c.length);
            cb.put(c);
            cb.flip();
            ByteBuffer bb = cs.encode(cb);
            return bb.array();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void main(String[] args) {
        new NIOLocalServer(4444);
    }
}
