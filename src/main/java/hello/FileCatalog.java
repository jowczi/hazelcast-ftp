package hello;

import java.io.InputStream;
import java.util.List;

public interface FileCatalog extends AutoCloseable {
    void init();
    List<String> listFileNames();
    InputStream fileContents(String fileName);
}
