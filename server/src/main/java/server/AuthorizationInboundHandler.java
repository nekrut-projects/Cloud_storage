package server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import shared.AbstractCommand;
import shared.AuthMessageRequest;
import shared.AuthMessageResponse;
import shared.RegMessageRequest;

import java.nio.file.Files;
import java.nio.file.Path;

public class AuthorizationInboundHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private final ServerDB serverDB;
    private Path rootDir;

    AuthorizationInboundHandler(ServerDB serverDB, Path rootDir){
        this.serverDB = serverDB;
        this.rootDir = rootDir;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand msg) throws Exception {

        if (msg instanceof RegMessageRequest) {
            RegMessageRequest regMessage = (RegMessageRequest) msg;
            String nameUserDir = serverDB.addUser(regMessage);
            if (nameUserDir != null){
                Files.createDirectory(rootDir.resolve(nameUserDir));
            }
        } else if (msg instanceof AuthMessageRequest) {
            AuthMessageRequest authMessage = (AuthMessageRequest) msg;
            String userDir = serverDB.getUserDir(authMessage.getUsername(), authMessage.getPassword());
            if(userDir == null){
                ctx.writeAndFlush(new AuthMessageResponse(false));
                return;
            }

            ctx.pipeline()
                    .get(ObjectHandler.class)
                    .setCurrentDir(rootDir.resolve(userDir));

            ctx.writeAndFlush(new AuthMessageResponse(true));
            ctx.pipeline().remove(AuthorizationInboundHandler.class);
        }
    }
}


