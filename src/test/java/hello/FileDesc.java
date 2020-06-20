package hello;

import lombok.Value;

import java.io.Serializable;

@Value
public class FileDesc implements Serializable {
    private String dir;
    private String file;
    private String contents;

    public String getPath() {
        return dir + "/" + file;
    }
}
