package NIO.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class MyResponse implements Serializable {
    private String ResponseId;
    private HashMap HeaderMap;
    private String param;
    private int Code;
    private byte[] Content;
}
