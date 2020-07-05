package hello;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;

import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Math.abs;

public class DirectoryProcessor<T> extends AbstractProcessor {

    private final FileCatalog fileCatalog;
    private Function<InputStream, StatefulMapper<T>> mapperFactory;
    private Predicate<String> fileNameFilter;

    private Traverser<T> trav;

    public DirectoryProcessor(FileCatalog fileCatalog, MapperFactory<T> mapperFactory, Predicate<String> fileNameFilter) {
        this.fileCatalog = fileCatalog;
        this.mapperFactory = mapperFactory;
        this.fileNameFilter = fileNameFilter;
    }

    @Override
    protected void init(Context context) throws Exception {
        super.init(context);



        fileCatalog.init();
        Stream<T> stream = fileCatalog.listFileNames().stream()
                .filter(fileNameFilter)
                .filter(fileName -> abs(fileName.hashCode()) % context.totalParallelism() == context.globalProcessorIndex())
                .flatMap(fileName -> {
                    InputStream fileInputStream = fileCatalog.fileContents(fileName);
                    StatefulMapper<T> mapper = mapperFactory.apply(fileInputStream);
                    return mapper.get().onClose(
                            () -> mapper.close()
                    );
                });


        this.trav = Traversers.traverseStream(stream);
    }

    @Override
    public boolean complete() {
        return emitFromTraverser(trav);
    }

    @Override
    public void close() throws Exception {
//        mappers.forEach(StatefulMapper::close);
    }
}
