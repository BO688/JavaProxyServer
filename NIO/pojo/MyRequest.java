package NIO.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class MyRequest implements Serializable {
    private String requestId;
    private HashMap HeaderMap;
    private String param;
}
