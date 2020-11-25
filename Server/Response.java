package Server;

import java.io.*;
import java.util.Date;

public class Response {
    //空格
    private final String BLANK="  ";
    //换行
    private final String BR="\r\n";
    //显示Http响应头
    private StringBuffer responseData;
    //封装网页信息
    private StringBuffer responseInfo;
    //字符缓冲输出流
    private BufferedWriter bufferedWriter;
    //响应的长度
    private long len;
    //初始化配置
    public Response(OutputStream outputStream){
        responseData=new StringBuffer();
        responseInfo=new StringBuffer();
        bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream));
    }
    public Response(OutputStream outputStream,String encode) throws UnsupportedEncodingException {
        responseData=new StringBuffer();
        responseInfo=new StringBuffer();
        bufferedWriter=new BufferedWriter(new OutputStreamWriter(outputStream,encode));
    }
    //创建头部信息
    private void createHeadInfo(int code){
        responseData.append("HTTP/1.1");
        responseData.append(BLANK);
        responseData.append(code + "").append(BLANK);
        switch (code){
            case 200:
                responseData.append("OK");
                break;
            case 404:
                responseData.append("not found");
                break;
            case 500:
                responseData.append("server error");
                break;
            default:
                break;
        }
        responseData.append(BR);
        responseData.append("Date:").append(new Date()).append(BR);
        responseData.append("Content-Type: text/html;charset=UTF-8").append(BR);
        responseData.append("Content-Length:").append(len).append(BR);
        responseData.append("Accept:").append("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").append(BR);
        responseData.append("Accept-Language:").append("zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2").append(BR);
        responseData.append("Accept-Encoding: gzip, deflate").append(BR);
        responseData.append("Connection: keep-alive").append(BR);
        responseData.append("Server: BWS/1.1").append(BR);
        responseData.append(BR);
    }

    /**
     * 填充页面类容
     * @param str
     * @return
     */
    public Response print(String str) {
        responseInfo.append(str);
        len += str.getBytes().length;
        return this;
    }
    /**
     * 发送数据
     * @throws IOException
     */
    public void connect(int code) throws IOException {
        createHeadInfo(code);
        bufferedWriter.append(responseData.toString());
        bufferedWriter.append(responseInfo.toString());
        bufferedWriter.flush();
//        //关闭
        bufferedWriter.close();
    }
    public void connectWithOutHeader(int code) throws IOException {
        bufferedWriter.append(responseData.toString());
        bufferedWriter.append(responseInfo.toString());
        bufferedWriter.flush();
//        //关闭
        bufferedWriter.close();
    }
    public void connect(StringBuffer Header,StringBuffer Content) throws IOException {
        bufferedWriter.append(Header.toString());
        bufferedWriter.append(Content.toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }
    public void connect(StringBuffer[] HeaderAndContent) throws IOException {
        bufferedWriter.append("HTTP/1.1 200 OK\n" +
                "Server:bfe\n" +
                "Content-Length:2381\n" +
                "Date:Sat, 03 Oct 2020 05:06:05 GMT\n" +
                "Content-Type:text/html");
        bufferedWriter.append(HeaderAndContent[1].toString());
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}
