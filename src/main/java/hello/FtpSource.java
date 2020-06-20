package hello;

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
    public static <T> BatchSource<T> fromDirectory(String directory, Predicate<String> fileNameFilter, ConnectionParams params, MapperFactory<T> mapperFactory){

        FileCatalog ftpFileCatalog = new FtpFileCatalog(directory, params);
        return Sources.batchFromProcessor("files in dir "+directory, ProcessorMetaSupplier.preferLocalParallelismOne(() -> new DirectoryProcessor(ftpFileCatalog, mapperFactory)) );
    }

    public static <T> BatchSource<T> fromDirectory(String directory, FileCatalog fileCatalog, Predicate<String> fileNameFilter, MapperFactory<T> mapperFactory){

        return Sources.batchFromProcessor("files in dir "+directory, ProcessorMetaSupplier.of(() -> new DirectoryProcessor(fileCatalog, mapperFactory)) );
    }



}
