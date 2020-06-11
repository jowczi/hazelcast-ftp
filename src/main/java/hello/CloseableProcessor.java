package hello;


import com.hazelcast.jet.core.AbstractProcessor;

public class CloseableProcessor extends AbstractProcessor {

    private final AbstractProcessor delegate;
    private final AutoCloseable closeable;

    public CloseableProcessor(AbstractProcessor delegate, AutoCloseable closeable) {
        this.delegate = delegate;
        this.closeable = closeable;
    }

    @Override
    public boolean complete() {
        return delegate.complete();
    }

    @Override
    public void close() throws Exception {
        closeable.close();
    }
}
