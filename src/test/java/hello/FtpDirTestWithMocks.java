package hello;

import com.google.common.io.Closeables;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import lombok.*;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.sfm.csv.CsvWriter;

import java.io.*;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class FtpDirTestWithMocks extends JetTestSupport {

    private MockFtpServerHandler ftpServer = new MockFtpServerHandler();


    @Test
    public void shouldProcessDirectory() throws Exception {
        //given
        ftpServer.addFile(new FileDesc("/csv", "file1", serialize(new TestType("a1", "b1", "c1"))));
        BatchSource<FtpDirTest.TestType> source = FtpSource.fromDirectory("/csv", ftpServer.getCatalog("/csv"), a -> true, is -> new CsvInputStreamMapper<>(is, FtpDirTest.TestType.class));

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


        BatchSource<FtpDirTest.TestType> source = FtpSource.fromDirectory("/multiple", ftpServer.getCatalog("/multiple"), a -> true, is -> new CsvInputStreamMapper<>(is, FtpDirTest.TestType.class));

        // when
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source).writeTo(Sinks.list("output"));
        JetInstance jet = createJetMember();
        jet.newJob(pipeline).join();
        //then
        List<FtpDirTest.TestType> result = jet.getList("output");
        List<FtpDirTest.TestType> expected = IntStream.rangeClosed(1, 3).mapToObj(i -> new FtpDirTest.TestType("val1_" + i, "val2_" + i, "val3_" + i)).collect(toList());
        Assertions.assertThat(result).containsExactlyElementsOf(expected);

    }

    @Test
    public void shouldProcessAllFilesOnceOnMultipleMembers() throws Exception {
        //given
        for(int i=1; i<= 3; i++) {
            ftpServer.addFile(new FileDesc("/multiple", "file"+i, serialize(new TestType("val1_"+i, "val2_"+i, "val3_"+i))));
        }


        BatchSource<FtpDirTest.TestType> source = FtpSource.fromDirectory("/multiple", ftpServer.getCatalog("/multiple"), a -> true, is -> new CsvInputStreamMapper<>(is, FtpDirTest.TestType.class));

        // when
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source).writeTo(Sinks.list("output"));
        JetInstance[] jets = createJetMembers(3);
        jets[0].newJob(pipeline).join();
        //then
        List<FtpDirTest.TestType> result = jets[0].getList("output");
        List<FtpDirTest.TestType> expected = IntStream.rangeClosed(1, 3).mapToObj(i -> new FtpDirTest.TestType("val1_" + i, "val2_" + i, "val3_" + i)).collect(toList());
        Assertions.assertThat(result).containsAll(expected);

    }

    @Test
    public void shouldFilterFiles() throws Exception {
        //given
        for(int i=1; i<= 100; i++) {
            ftpServer.addFile(new FileDesc("/multiple", "file"+i, "line"+i));
        }
        for(int i=1; i<= 10; i++) {
            ftpServer.addFile(new FileDesc("/multiple", "ignored"+i, "ignored"+i));
        }

        MapperFactory<String> linesInFile = (is) -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            return () -> reader.lines().onClose(() -> Closeables.closeQuietly(reader));
        };

        BatchSource<String> source = FtpSource.fromDirectory("/multiple", ftpServer.getCatalog("/multiple"), fileName -> fileName.startsWith("file"), linesInFile);

        // when
        Pipeline pipeline = Pipeline.create();
        pipeline.readFrom(source).writeTo(Sinks.list("output"));
        JetInstance jet = createJetMember();
        jet.newJob(pipeline).join();
        //then
        List<String> result = jet.getList("output");
        List<String> expected = IntStream.rangeClosed(1, 100).mapToObj(i -> "line"+i).collect(toList());
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
