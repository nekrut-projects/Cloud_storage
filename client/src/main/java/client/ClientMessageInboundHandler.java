package client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import shared.AbstractCommand;

public class ClientMessageInboundHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private final Callback callback;

    public ClientMessageInboundHandler(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand message) throws Exception {
        callback.call(message);
    }
}
