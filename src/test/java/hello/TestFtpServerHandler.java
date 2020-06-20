package hello;

import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.stub.command.AbstractStubDataCommandHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestFtpServerHandler implements FtpServer{

    StubFtpServer server = new StubFtpServer();

    List<FileDesc> files = new ArrayList<>();

    public void start() {
        server.start();
        server.setCommandHandler("RETR", new AbstractStubDataCommandHandler() {
            @Override
            protected void processData(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
                retr(command, session);
            }
        });
        server.setCommandHandler("NLST", new AbstractStubDataCommandHandler() {
            @Override
            protected void processData(Command command, Session session, InvocationRecord invocationRecord) throws Exception {
                list(command, session);
            }
        });
    }

    @Override
    public void addFile(FileDesc fileDesc) {
        files.add(fileDesc);
    }

    private void list(Command command, Session session) {
        String dir = command.getRequiredParameter(0);
        String reply = this.files.stream()
                .filter(f -> f.getDir().equals(dir))
                .map(f -> f.getFile())
                .collect(Collectors.joining(System.lineSeparator()));

        session.sendData(reply.getBytes(StandardCharsets.UTF_8), reply.length());
    }


    private void retr(Command command, Session session) {
        String path = command.getRequiredParameter(0);
        Optional<FileDesc> fileDesc = files.stream()
                .filter(fileEntry -> fileEntry.getPath().equals(path))
                .findFirst();
        if (fileDesc.isPresent()) {
            fileDesc.ifPresent(fileEntry -> session.sendData(fileEntry.getContents().getBytes(StandardCharsets.UTF_8), fileEntry.getContents().length()));
        } else {
            session.sendReply(404, "");
        }

    }



    @Override
    public void addDir(DirectoryEntry directoryEntry) {

    }

    @Override
    public void stop() {
        server.stop();
    }

    @Override
    public ConnectionParams connectionParams() {
        return new ConnectionParams("", "", "localhost", server.getServerControlPort());
    }
}
