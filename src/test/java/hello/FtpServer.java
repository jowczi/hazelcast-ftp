package hello;

import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;

import java.io.IOException;

public interface FtpServer {
    void start() throws IOException;

    void addFile(FileDesc fileDesc);

    void addDir(DirectoryEntry directoryEntry);

    void stop();

    ConnectionParams connectionParams();
}
