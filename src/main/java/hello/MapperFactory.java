package hello;

import java.io.InputStream;
import java.io.Serializable;
import java.util.function.Function;

public interface MapperFactory<T> extends Function<InputStream, StatefulMapper<T>>, Serializable {
}
