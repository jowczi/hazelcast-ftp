package hello;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class InputStreamSource extends AbstractProcessor{

    private final Traverser<?> trav;
    private final BufferedReader reader;

    public InputStreamSource(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
        this.trav = Traversers.traverseStream(reader.lines());
    }

    @SneakyThrows
    public boolean complete() {
        return this.emitFromTraverser(this.trav);
    }

    public static <T> ProcessorMetaSupplier supplier(Stream<T> stream) {
        return ProcessorMetaSupplier.preferLocalParallelismOne(() -> new StreamSource(stream));
    }

}
