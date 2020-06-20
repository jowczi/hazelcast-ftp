package hello;

import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class DirectoryProcessor<T> extends AbstractProcessor {

    private final FileCatalog fileCatalog;
    private Function<InputStream, StatefulMapper<T>> mapperFactory;
    private transient List<StatefulMapper<T>> mappers = new ArrayList<>();

    private Traverser<T> trav;

    public DirectoryProcessor(FileCatalog fileCatalog, MapperFactory<T> mapperFactory) {
        this.fileCatalog = fileCatalog;
        this.mapperFactory = mapperFactory;

    }

    @Override
    protected void init(Context context) throws Exception {
        super.init(context);



        fileCatalog.init();
        Stream<T> stream = fileCatalog.listFileNames().stream()
                .filter(fileName -> fileName.hashCode() % context.totalParallelism() == context.globalProcessorIndex())
                .flatMap(fileName -> {
                    InputStream fileInputStream = fileCatalog.fileContents(fileName);
                    StatefulMapper<T> mapper = mapperFactory.apply(fileInputStream);
                    mappers.add(mapper);
                    return mapper.get();
                });


        this.trav = Traversers.traverseStream(stream);
    }

    @Override
    public boolean complete() {
        return emitFromTraverser(trav);
    }

    @Override
    public void close() throws Exception {
        mappers.forEach(StatefulMapper::close);
    }
}
