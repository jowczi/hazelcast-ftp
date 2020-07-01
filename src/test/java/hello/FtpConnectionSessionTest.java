package hello;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FtpConnectionSessionTest {

    @Test
    public void testMultipleTransfers() throws Exception {
        //https://stackoverflow.com/questions/13191052/can-ftp-have-multiple-tcp-connection-for-multiple-parallel-file-transfer
        FtpServer ftpServer = new TestFtpServerHandler();
        ftpServer.start();
        ftpServer.addFile(new FileDesc("/dir", "file1", "hello"));
        ftpServer.addFile(new FileDesc("/dir", "file2", "hello"));

        FtpFileCatalog catalog = new FtpFileCatalog("/dir", ftpServer.connectionParams());

        catalog.init();
        InputStream stream1 = catalog.fileContents("file1");
        System.out.println(IOUtils.readLines(stream1, StandardCharsets.UTF_8));
        stream1.close();
        InputStream stream2 = catalog.fileContents("file2");
        System.out.println(IOUtils.readLines(stream2, StandardCharsets.UTF_8));

        ftpServer.stop();
    }
}
