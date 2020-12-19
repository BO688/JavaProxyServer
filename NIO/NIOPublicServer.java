package NIO;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.util.HashMap;
import java.util.UUID;

public class NIOPublicServer {
    public static HashMap<String,ChannelHandlerContext> ResponseMap=new HashMap<>();
    NIOPublicServer(int port){
        new NIOServerClient(4444).start();
        EventLoopGroup elp=new NioEventLoopGroup();
        try {
            ServerBootstrap b=new ServerBootstrap();
            b.group(elp)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            ch.pipeline().addLast("codec", new HttpServerCodec());
                            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(1048576));
                            ch.pipeline().addLast("serverHandler", new SimpleChannelInboundHandler<FullHttpRequest>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
                                    this.readRequest(ctx,msg);
                                }
                                private void readRequest(ChannelHandlerContext ctx,FullHttpRequest msg) throws InterruptedException {
//                                    System.out.println("======请求行======");
//                                    System.out.println(msg.method() + " " + msg.uri() + " " + msg.protocolVersion());
                                    String ID=UUID.randomUUID().toString();
                                    ResponseMap.put(ID,ctx);
                                    new MyNioThread(msg.uri(),ID).start();
                                }
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                    System.out.println("ServerClient");
                                    ctx.close();
                                    cause.printStackTrace();
                                }
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync();
            System.out.println("HttpServer已启动，端口：" + port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅的关闭
            elp.shutdownGracefully();

        }
    }
    public static void main(String[] args) {
        new NIOPublicServer(80);

    }
}

