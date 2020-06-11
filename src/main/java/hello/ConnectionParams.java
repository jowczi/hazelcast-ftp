package hello;

import lombok.Value;

import java.io.Serializable;

@Value
public class ConnectionParams implements Serializable {
    private String user;
    private String password;
    private String server;
    private int port;
}
