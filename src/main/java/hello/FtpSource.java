package hello;

import com.hazelcast.function.PredicateEx;
import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Sources;

import java.io.InputStream;
import java.util.function.Function;
import java.util.function.Predicate;

public class FtpSource {

    public static BatchSource<String> singleFile(String filePath, ConnectionParams params){

        return Sources.batchFromProcessor("single file processor "+filePath, ProcessorMetaSupplier.of(() -> new FtpFileProcessor(filePath, params)) );
    }
    public static <T> BatchSource<T> fromDirectory(String directory, PredicateEx<String> fileNameFilter, ConnectionParams params, MapperFactory<T> mapperFactory){

        FileCatalog ftpFileCatalog = new FtpFileCatalog(directory, params);
        return Sources.batchFromProcessor("files in dir "+directory, ProcessorMetaSupplier.preferLocalParallelismOne(() -> new DirectoryProcessor(ftpFileCatalog, mapperFactory, fileNameFilter)) );
    }

    public static <T> BatchSource<T> fromDirectory(String directory, FileCatalog fileCatalog, PredicateEx<String> fileNameFilter, MapperFactory<T> mapperFactory){

        return Sources.batchFromProcessor("files in dir "+directory, ProcessorMetaSupplier.of(() -> new DirectoryProcessor(fileCatalog, mapperFactory, fileNameFilter)) );
    }



}
