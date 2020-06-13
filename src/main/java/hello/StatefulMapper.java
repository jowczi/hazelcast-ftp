package hello;

import java.util.function.Supplier;
import java.util.stream.Stream;

public interface StatefulMapper<T> extends Supplier<Stream<T>>, Initializable, AutoCloseable {

    @Override
    void close();
}
