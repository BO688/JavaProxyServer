package NIO;

import NIO.pojo.MyResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.HashMap;

public class NIOServerClient extends Thread{
    public static HashMap<String, MyResponse> responseByteBuf=new HashMap<>();
    int port ;
    EventLoopGroup elp;
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    class MyChannel extends ChannelInitializer<Channel> {
        @Override
        protected void initChannel(Channel ch) {
            ch.pipeline().addLast(
                    new ObjectDecoder(1024 *1024*1024, ClassResolvers
                            .cacheDisabled(getClass().getClassLoader())));
            ch.pipeline().addLast(new ObjectEncoder());
            ch.pipeline().addLast("serverHandler", new ChannelInboundHandlerAdapter() {
                @Override
                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                    channels.add(ctx.channel());
                }
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    MyResponse myResponse=(MyResponse)msg;
                    responseByteBuf.put(myResponse.getResponseId(),myResponse);
                }
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                    ctx.close();
                    System.out.println("ServerClient");
                    cause.printStackTrace();
                }
                public String TrimSpace(String param){
                    while (param.startsWith(" ")){
                        param=param.substring(1);
                    }
                    while (param.endsWith(" ")){
                        param=param.substring(0,param.length()-1);
                    }
                    return param;
                }
            });
        }
    }
    NIOServerClient(int port){
        this.port=port;
    }
    @Override
    public void run() {
      elp =new NioEventLoopGroup();
        try {
            ServerBootstrap b=new ServerBootstrap();
            b.group(elp)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new MyChannel())
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();
            System.out.println("ServerClient已启动，端口：" + port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅的关闭
            elp.shutdownGracefully();
        }
    }
}

