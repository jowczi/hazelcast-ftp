package hello;

import lombok.SneakyThrows;
import org.sfm.csv.CsvMapper;
import org.sfm.csv.CsvMapperFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Stream;

public class CsvInputStreamMapper<T> implements StatefulMapper<T> {

    private InputStreamReader inputStreamReader;
    private Class<T> targetClass;
    private CsvMapper<T> mapper;

    public CsvInputStreamMapper(InputStream inputStream, Class<T> targetClass) {
        this.inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
        this.targetClass = targetClass;
        mapper = CsvMapperFactory.newInstance().newMapper(targetClass);
    }

    @Override
    @SneakyThrows
    public void close() {
        inputStreamReader.close();

    }

    @SneakyThrows
    @Override
    public Stream<T> get() {
        return mapper.stream(inputStreamReader);
    }
}
