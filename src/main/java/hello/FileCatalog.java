package hello;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

public interface FileCatalog extends AutoCloseable, Serializable {
    void init();
    List<String> listFileNames();
    InputStream fileContents(String fileName);
}
