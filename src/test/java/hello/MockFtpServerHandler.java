package hello;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockFtpServerHandler implements Serializable {

    List<FileDesc> files = new ArrayList<>();

    public void addFile(FileDesc fileDesc) {
        files.add(fileDesc);
    }



    public FileCatalog getCatalog(String dir) {
        return new FileCatalog() {
            @Override
            public void close() throws Exception {

            }

            @Override
            public void init() {

            }

            @Override
            public List<String> listFileNames() {
                return files.stream()
                        .filter(f -> f.getDir().equals(dir))
                        .map(f -> f.getFile())
                        .collect(Collectors.toList());
            }

            @Override
            public InputStream fileContents(String fileName) {
                return files.stream()
                        .filter(fileEntry -> fileEntry.getFile().equals(fileName))
                        .findFirst()
                        .map(fileDesc -> new ByteArrayInputStream(fileDesc.getContents().getBytes(StandardCharsets.UTF_8)))
                        .orElse(null);

            }
        };
    }
}
