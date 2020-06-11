package hello;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;
import com.hazelcast.jet.core.ProcessorMetaSupplier;

import java.util.stream.Stream;

public class StreamSource<T> extends AbstractProcessor {
    private final Traverser<?> trav;
    private Runnable closeCallback;

    public StreamSource(Stream<T> stream) {
        this.trav = Traversers.traverseStream(stream);
    }

    public StreamSource(Stream<T> stream, Runnable closeCallback) {
        this.trav = Traversers.traverseStream(stream);
        this.closeCallback = closeCallback;
    }


    public boolean complete() {
        return this.emitFromTraverser(this.trav);

    }

    public static <T> ProcessorMetaSupplier supplier(Stream<T> stream) {
        return ProcessorMetaSupplier.preferLocalParallelismOne(() -> new StreamSource(stream));
    }

    @Override
    public void close() throws Exception {
        if (closeCallback != null) {
            closeCallback.run();
        }
    }
}