package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.java.Log;
import shared.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Log
public class ObjectHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private final String NAME_NEW_DIR = "New folder";
    private final String SEPARATOR = " ! ";
    private String userDir;

    private Path currentDir;

    public void setCurrentDir(Path userDir) {
        this.currentDir = userDir;
        this.userDir = userDir.getFileName().toString();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand message) throws Exception {
        if (message instanceof FilesListRequest) {
            ctx.writeAndFlush(getFiles());
        }
        if (message instanceof FileRequest) {
            sendToClient(ctx, ((FileRequest) message).getName());
        }
        if (message instanceof FileMessage) {
            saveFile((FileMessage) message);
        }
        if (message instanceof ControlCommand) {
            ControlCommand msg = (ControlCommand) message;
            switch (msg.getCommand()){
                case DELETE_FILE:
                    deleteFile(msg.getFileName());
                    break;
                case OPEN_FOLDER:
                    goToPath(msg.getFileName());
                    break;
                case GO_TO_UPPER_FOLDER:
                    goToUpperFolder();
                    break;
                case CREATE_FOLDER:
                    createFolder(msg.getFileName());
                    break;
                case RENAME_FILE:
                    renameFile(msg.getFileName());
                    break;

            }
        }
    }

    private void renameFile(String fileName) {
        String[] names = fileName.split(SEPARATOR);
        String oldName = names[0];
        String newName = names[1];

        Path source = currentDir.resolve(oldName);
        Path target = source.resolveSibling(newName);
        try {
            Files.move(source, target);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendToClient(ChannelHandlerContext ctx, String filename) {
        Path path = currentDir.resolve(filename);
        if (Files.isDirectory(path)){
            sendDirectory(ctx, path);
        } else {
            sendFile(ctx, path);
        }
    }

    private void createFolder(String folderName) {
        if (folderName == null) {
            folderName = NAME_NEW_DIR;
            if (Files.exists(currentDir.resolve(folderName))) {
                folderName = generateNameNewFolder(folderName);
            }
        } else {
            if (Files.exists(currentDir.resolve(folderName))){
                return;
            }
        }
        try {
            Files.createDirectory(currentDir.resolve(folderName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateNameNewFolder(String folderName){
        for (int i = 1; ; i++) {
            String temp = "(" + i + ")";
            if (!Files.exists(currentDir.resolve(folderName + temp))) {
                return folderName + temp;
            }
        }
    }

    private void deleteFile(String filename) {
        Path path = currentDir.resolve(filename);
        try {
            if (Files.isDirectory(path)){
                Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        log.info("Delete file: " + file.normalize().toAbsolutePath().toString());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        log.info("Delete directory: " + dir.normalize().toAbsolutePath().toString());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void goToUpperFolder() {
        if (currentDir.getFileName().toString().equals(userDir)){
            return;
        }
        currentDir = currentDir.toAbsolutePath().getParent();
    }

    private void goToPath(String fileName) {
        currentDir = currentDir.resolve(fileName);
    }

    private void saveFile(FileMessage fileMessage) throws IOException {
        Files.write(
                currentDir.resolve(fileMessage.getName()),
                fileMessage.getContent(),
                StandardOpenOption.CREATE
        );
        log.info("Uploaded to server file: " + fileMessage.getName());
    }

    private FilesListResponse getFiles() throws IOException {
        return new FilesListResponse(currentDir);
    }

    private void sendDirectory(ChannelHandlerContext ctx, Path dir){
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    sendFile(ctx, file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String nameDir = dir.getFileName().toString();
                    ctx.writeAndFlush(new ControlCommand(ControlCommand.Type.CREATE_FOLDER, nameDir));
                    ctx.writeAndFlush(new ControlCommand(ControlCommand.Type.OPEN_FOLDER, nameDir));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    ctx.writeAndFlush(new ControlCommand(ControlCommand.Type.GO_TO_UPPER_FOLDER, dir.getFileName().toString()));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFile(ChannelHandlerContext ctx, Path path){
        try {
            ctx.writeAndFlush(new FileMessage(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
