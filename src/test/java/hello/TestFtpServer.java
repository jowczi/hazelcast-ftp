package hello;

import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.IOException;
import java.util.stream.IntStream;

public class TestFtpServer implements FtpServer {

    private FakeFtpServer fakeFtpServer;

    @Override
    public void start() throws IOException {

        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.addUserAccount(new UserAccount("user", "password", "/dir"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/dir"));
        fileSystem.add(new FileEntry("/dir/file.txt", IntStream.range(100, 500).mapToObj(i -> "line" + i + System.lineSeparator()).reduce("", String::concat)));

        fileSystem.add(new DirectoryEntry("/csv"));
        fileSystem.add(new FileEntry("/csv/file.txt", "col1,col2,col3\r\n" +
                "a1,b1,c1"));

        fakeFtpServer.setFileSystem(fileSystem);
        fakeFtpServer.setServerControlPort(0);

        fakeFtpServer.start();


    }

    @Override
    public void addFile(FileDesc fileDesc) {
        addFile(new FileEntry(fileDesc.getPath(), fileDesc.getContents()));
    }

    public void setFileSystem(FileSystem fileSystem) {
        fakeFtpServer.setFileSystem(fileSystem);
    }

    public void addFile(FileEntry fileEntry) {
        fakeFtpServer.getFileSystem().add(fileEntry);
    }
    @Override
    public void addDir(DirectoryEntry directoryEntry) {
        fakeFtpServer.getFileSystem().add(directoryEntry);
    }

    @Override
    public void stop(){
        fakeFtpServer.stop();
    }

    @Override
    public ConnectionParams connectionParams() {
        return new ConnectionParams("user", "password", "localhost", fakeFtpServer.getServerControlPort());
    }


}
