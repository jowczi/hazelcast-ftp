package hello;

import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.sfm.csv.CsvWriter;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FtpDirTestWithServerHandler extends JetTestSupport {

    private FtpServer ftpServer = new TestFtpServerHandler();

    @Before
    public void setup() throws IOException {
        ftpServer.start();
    }

    @After
    public void teardown() throws IOException {
        ftpServer.stop();
    }

    @Test
    public void name() throws Exception {
        ftpServer.addFile(new FileDesc("/csv", "file1", "hello"));
        FtpFileCatalog ftpFileCatalog = new FtpFileCatalog("/csv", ftpServer.connectionParams());
        ftpFileCatalog.init();
        System.out.println(ftpFileCatalog.listFileNames());
        System.out.println(new BufferedReader(new InputStreamReader(ftpFileCatalog.fileContents("file.txt"))).lines().collect(Collectors.toList()));
    }

    @Test
    public void shouldProcessDirectory() throws Exception {
        //given
        ftpServer.addDir(new DirectoryEntry("/csv"));
        ftpServer.addFile(new FileDesc("/csv", "file1", serialize(new TestType("a1", "b1", "c1"))));
        BatchSource<FtpDirTest.TestType> source = FtpSource.fromDirectory("/csv", a -> true, ftpServer.connectionParams(), is -> new CsvInputStreamMapper<>(is, FtpDirTest.TestType.class));

        // when
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source).writeTo(Sinks.list("output"));
        JetInstance jet = createJetMember();
        jet.newJob(pipeline).join();
        //then
        List<FtpDirTest.TestType> result = jet.getList("output");
        Assertions.assertThat(result).containsOnly(new FtpDirTest.TestType("a1", "b1", "c1"));

    }

    @Test
    public void shouldProcessAllFilesOnce() throws Exception {
        //given
        for(int i=1; i<= 3; i++) {
            ftpServer.addFile(new FileDesc("/multiple", "file"+i, serialize(new TestType("val1_"+i, "val2_"+i, "val3_"+i))));
        }


        BatchSource<FtpDirTest.TestType> source = FtpSource.fromDirectory("/multiple", a -> true, ftpServer.connectionParams(), is -> new CsvInputStreamMapper<>(is, FtpDirTest.TestType.class));

        // when
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source).writeTo(Sinks.list("output"));
        JetInstance jet = createJetMember();
        jet.newJob(pipeline).join();
        //then
        List<FtpDirTest.TestType> result = jet.getList("output");
        List<FtpDirTest.TestType> expected = IntStream.rangeClosed(1, 3).mapToObj(i -> new FtpDirTest.TestType("val1_" + i, "val2_" + i, "val3_" + i)).collect(Collectors.toList());
        Assertions.assertThat(result).containsAll(expected);

    }

    private String serialize(TestType object) throws IOException {
        StringWriter stringWriter = new StringWriter();
        CsvWriter<TestType> writer = CsvWriter.from(TestType.class).to(stringWriter);
        writer.append(object);
        return stringWriter.toString();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestType implements Serializable {
        private String col1, col2, col3;
    }
}
