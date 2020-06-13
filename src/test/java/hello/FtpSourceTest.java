package hello;

import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.PipelineTestSupport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;


public class FtpSourceTest extends PipelineTestSupport {


    private TestFtpServer testFtpServer = new TestFtpServer();

    @Before
    public void setup() throws IOException {
        testFtpServer.start();
    }

    @After
    public void teardown() throws IOException {
        testFtpServer.stop();
    }

    @Test
    public void shouldRead() throws Exception {

        //given
        BatchSource<String> source = FtpSource.singleFile("/dir/file.txt", connectionParams());

        // when
        p.readFrom(source).writeTo(sink);
        execute();
        //then
        Collection<String> expected = IntStream.range(100,500).mapToObj(i -> "line"+i).collect(Collectors.toList());
        assertEquals(expected, sinkToBag().keySet().stream().sorted().collect(Collectors.toList()));
    }

    private ConnectionParams connectionParams() {
        return testFtpServer.connectionParams();
    }

    @Test
    public void shouldProcessDirectory() throws Exception {
        //given
        BatchSource<TestType> source = FtpSource.fromDirectory("/csv", a -> true, connectionParams(), is -> new CsvInputStreamMapper<>(is, TestType.class));
        // when
        p.readFrom(source).writeTo(sink);
        execute();
        //then
        Map<TestType, Integer> result = sinkToBag();
        Assertions.assertThat(result.keySet()).containsOnly(new TestType("a1", "b1", "c1"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestType implements Serializable {
        private String col1, col2, col3;
    }
}
