package hello;

import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.PipelineTestSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;


public class FtpSourceTest extends PipelineTestSupport {


    private FakeFtpServer fakeFtpServer;

    @Before
    public void setup() throws IOException {

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/dir"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/dir"));
        fileSystem.add(new FileEntry("/dir/file.txt", IntStream.range(100, 500).mapToObj(i -> "line" + i + System.lineSeparator()).reduce("", String::concat)));
        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.start();


    }

    @After
    public void teardown() throws IOException {
        fakeFtpServer.stop();
    }

    @Test
    public void shouldRead() throws Exception {

        //given
        BatchSource<String> source = FtpSource.ftp("/dir/file.txt", new ConnectionParams("user", "password", "localhost", fakeFtpServer.getServerControlPort()));

        // when
        p.readFrom(source).writeTo(sink);
        execute();
        //then
        Collection<String> expected = IntStream.range(100,500).mapToObj(i -> "line"+i).collect(Collectors.toList());
        assertEquals(expected, sinkToBag().keySet().stream().sorted().collect(Collectors.toList()));
    }
}
