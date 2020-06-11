package hello;

import lombok.SneakyThrows;
import org.apache.commons.net.ftp.FTPClient;

import java.io.InputStream;

public class FtpConnection implements AutoCloseable {

    private ConnectionParams params;
    private FTPClient f;

    public FtpConnection(ConnectionParams params) {
        this.params = params;
        connect();
    }

    @SneakyThrows
    private void connect() {
        f = new FTPClient();
        f.connect(params.getServer(), params.getPort());
        f.login(params.getUser(), params.getPassword());
    }

    @SneakyThrows
    public InputStream fileStream(String filePath){
        return f.retrieveFileStream(filePath);
    }


    @SneakyThrows
    @Override
    public void close() {
        f.logout();
    }
}
