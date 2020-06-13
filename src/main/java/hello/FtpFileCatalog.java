package hello;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

public class FtpFileCatalog implements FileCatalog, Serializable {

    private final String directory;
    private final ConnectionParams params;
    private transient FtpConnection connection;

    public FtpFileCatalog(String directory, ConnectionParams params) {
        this.directory = directory;
        this.params = params;
    }


    @Override
    public void init() {
        connection = new FtpConnection(params);
    }

    @Override
    public List<String> listFileNames() {
        return connection.listFilesNames(directory);
    }

    @Override
    public InputStream fileContents(String fileName) {
        return connection.fileStream(directory+"/"+fileName);
    }

    @Override
    public void close() throws Exception {

    }
}
