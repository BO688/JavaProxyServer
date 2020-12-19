package NIO;

import NIO.pojo.MyRequest;
import NIO.pojo.MyResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class  MyNioThread extends Thread{
    String param;
    String RequestId;
    private FullHttpResponse res;

    MyNioThread(String param,String ID){
        this.param=param;
        this.RequestId=ID;
    }
    @Override
    public void run() {
        {
            try {
                MyRequest myRequest=new MyRequest();
                myRequest.setParam(param);
                myRequest.setRequestId(RequestId);
                NIOServerClient.channels.writeAndFlush(myRequest);
                int count=100;
                while (NIOServerClient.responseByteBuf.get(RequestId)==null&&count>0){
                    Thread.sleep(100);
                    count--;
                }
                System.out.println( NIOServerClient.responseByteBuf.get(RequestId).getParam());
                System.out.println( NIOServerClient.responseByteBuf.get(RequestId).getCode());

                if(count<=0){
                    HttpResponseStatus status=HttpResponseStatus.valueOf(200);
                    writeResponse(param,NIOPublicServer.ResponseMap.get(RequestId),"".getBytes(),status);
                }else{
                    MyResponse myResponse=NIOServerClient.responseByteBuf.get(RequestId);
                    HttpResponseStatus status=HttpResponseStatus.valueOf(myResponse.getCode());
                    writeResponse(param,NIOPublicServer.ResponseMap.get(RequestId),myResponse.getContent(),status);
                }
                NIOServerClient.responseByteBuf.remove(RequestId);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void writeResponse(String param, ChannelHandlerContext ctx, byte [] msg, HttpResponseStatus status) throws UnsupportedEncodingException {
        ByteBuf bf = Unpooled.wrappedBuffer(msg);
//        System.out.println(bf.toString(CharsetUtil.UTF_8));
        FullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, bf);
        res.headers().set(HttpHeaderNames.DATE,new Date());
        res.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.length);
        if(param.endsWith(".png")||param.endsWith(".jpg")||param.endsWith(".jpeg")||param.endsWith(".gif")){
            res.headers().set(HttpHeaderNames.LAST_MODIFIED,new Date());

        }else{
            res.headers().set(HttpHeaderNames.CONTENT_LENGTH, msg.length);
        }

        if(!param.contains(".")){
            res.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/html;charset=UTF-8");
        }

        ctx.writeAndFlush(res).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }
}