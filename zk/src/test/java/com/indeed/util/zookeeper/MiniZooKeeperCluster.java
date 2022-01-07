package com.indeed.util.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicBoolean;

/** @author jsgroth */
public class MiniZooKeeperCluster {
    private final Hack server;
    private final Thread zkServerThread;
    private final String zkNodes;

    public MiniZooKeeperCluster(final String dataDir) throws IOException {
        final int zkPort = getFreePort();
        zkNodes = "localhost:" + zkPort;

        server = new Hack();
        zkServerThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    server.initializeAndRun(
                                            new String[] {Integer.toString(zkPort), dataDir});
                                } catch (QuorumPeerConfig.ConfigException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
        zkServerThread.setDaemon(true);
        zkServerThread.start();

        waitForServerStartup();
    }

    public String getZkNodes() {
        return zkNodes;
    }

    public void shutdown() {
        server.shutdown();
        long startTime = System.currentTimeMillis();
        while (zkServerThread.isAlive() && (System.currentTimeMillis() - startTime) < 6000) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (zkServerThread.isAlive()) {
            throw new RuntimeException("failed to stop zookeeper server");
        }
    }

    private void waitForServerStartup() throws IOException {
        final AtomicBoolean connected = new AtomicBoolean(false);
        final ZooKeeper zk =
                new ZooKeeper(
                        zkNodes,
                        30000,
                        new Watcher() {
                            @Override
                            public void process(WatchedEvent watchedEvent) {
                                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                                    connected.set(true);
                                }
                            }
                        });
        try {
            long start = System.currentTimeMillis();
            while (!connected.get() && (System.currentTimeMillis() - start) < 30000) {}
            if (!connected.get()) {
                throw new RuntimeException("failed to connect to zookeeper");
            }
        } finally {
            try {
                zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static int getFreePort() throws IOException {
        final ServerSocket ss = new ServerSocket(0);
        final int port = ss.getLocalPort();
        ss.close();
        return port;
    }

    private static class Hack extends ZooKeeperServerMain {
        @Override
        public void initializeAndRun(String[] args)
                throws QuorumPeerConfig.ConfigException, IOException {
            super.initializeAndRun(args);
        }

        @Override
        public void shutdown() {
            super.shutdown();
        }
    }
}
