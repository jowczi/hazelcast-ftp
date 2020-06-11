package hello;

import com.hazelcast.jet.core.ProcessorMetaSupplier;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.Sources;

public class FtpSource {

    public static BatchSource<String> ftp(String filePath, ConnectionParams params){

        return Sources.batchFromProcessor("", ProcessorMetaSupplier.of(() -> new FtpFileProcessor(filePath, params)) );
    }



}
