package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.java.Log;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

@Log
public class Server {
    private final String SERVER_ROOT_DIR = "server/src/main/resources/serverDir";
    private Path rootDir;
    private ServerDB serverDB;

    public Server() {
        serverDB = new ServerDB();
        rootDir = Paths.get(SERVER_ROOT_DIR);
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        try {
            serverDB.connect();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectEncoder(),
                                    new ObjectDecoder(200 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    new AuthorizationInboundHandler(serverDB, rootDir),
                                    new ObjectHandler()
                            );
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(8189).sync();
            log.info("server.Server started");
            channelFuture.channel().closeFuture().sync(); // block
        } catch (Exception e) {
            log.warning(e.getMessage());
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            try {
                serverDB.disconnect();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                log.warning(throwables.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
