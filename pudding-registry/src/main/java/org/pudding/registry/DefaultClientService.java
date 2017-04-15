package org.pudding.registry;

import org.apache.log4j.Logger;
import org.pudding.transport.protocol.Message;
import org.pudding.transport.api.Acceptor;
import org.pudding.transport.api.Channel;
import org.pudding.transport.api.Processor;
import org.pudding.transport.netty.NettyTransportFactory;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * The default implementation of {@link ClientService}.
 *
 * @author Yohann.
 */
public class DefaultClientService extends AcknowledgeManager implements ClientService {
    private static final Logger logger = Logger.getLogger(DefaultClusterService.class);

    // Process the client(provider/consumer) task
    private final Processor clientProcessor = new ClientProcessor();

    private final Acceptor acceptor = NettyTransportFactory.createTcpAcceptor();

    private Channel channel;

    private volatile boolean isShutdown = false;

    private final ExecutorService executor;

    public DefaultClientService(ExecutorService executor) {
        acceptor.withProcessor(clientProcessor);
        this.executor = executor;
    }

    @Override
    public Channel startRegistry(SocketAddress localAddress) {
        checkNotShutdown();

        try {
            synchronized (acceptor) {
                channel = acceptor.bind(localAddress);

                logger.info("start registry server, channel:" + channel);
                return channel;
            }
        } catch (InterruptedException e) {
            logger.warn("start registry failed: " + channel, e);
        }

        return null; // Never get here
    }

    @Override
    public void closeRegistry() {
        channel.close();

        logger.info("close registry, channel:" + channel);
        channel = null;
    }

    @Override
    public void shutdown() {
        closeRegistry();
        acceptor.shutdownGracefully();
        isShutdown = true;
    }

    private void checkNotShutdown() {
        if (isShutdown) {
            throw new IllegalStateException("the instance has shutdown");
        }
    }

    /**
     * The processor about client(provider/consumer).
     */
    private class ClientProcessor implements Processor {

        @Override
        public void handleMessage(Channel channel, Message holder) {

            executor.execute(new Runnable() {

                @Override
                public void run() {

                }
            });
        }

        @Override
        public void handleConnection(Channel channel) {
            // Noop
        }

        @Override
        public void handleDisconnection(Channel channel) {
            // Noop
        }
    }
}