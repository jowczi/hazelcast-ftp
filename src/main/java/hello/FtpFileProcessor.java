package hello;

import com.google.common.io.Closeables;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.core.AbstractProcessor;
import com.hazelcast.jet.core.Processor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FtpFileProcessor extends AbstractProcessor {

    private final String filePath;
    private final ConnectionParams params;
    private BufferedReader reader;
    private Traverser<String> trav;

    public FtpFileProcessor(String filePath, ConnectionParams params) {

        this.filePath = filePath;
        this.params = params;
    }

    @Override
    protected void init(Context context) throws Exception {
        super.init(context);

        FtpConnection connection = new FtpConnection(params);
        InputStream fileStream = connection.fileStream(filePath);

        reader = new BufferedReader(new InputStreamReader(fileStream, Charset.forName("UTF-8")));
        this.trav = Traversers.traverseStream(reader.lines().onClose(() -> Closeables.closeQuietly(reader)));
    }

    @Override
    public boolean complete() {
        return emitFromTraverser(trav);
    }
}
