package com.indeed.util.zookeeper;

import com.indeed.util.io.Files;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/** @author jsgroth */
public class TestZooKeeperConnection {
    private MiniZooKeeperCluster zk;
    private String tempDir;

    @Before
    public void startZK() throws IOException {
        tempDir = Files.getTempDirectory("asdf", "");
        zk = new MiniZooKeeperCluster(tempDir);
    }

    @After
    public void stopZK() {
        if (zk != null) {
            zk.shutdown();
        }
        Files.delete(tempDir);
    }

    @Test
    public void testConnect() throws IOException, InterruptedException {
        String zkNodes = zk.getZkNodes();
        ZooKeeperConnection zkc = new ZooKeeperConnection(zkNodes, 30000);
        zkc.connect();
        assertSame(ZooKeeper.States.CONNECTED, zkc.getState());
        zkc.close();
    }

    @Test(expected = IOException.class)
    public void testFailConnect() throws IOException, InterruptedException {
        String zkNodes = zk.getZkNodes();
        zk.shutdown();
        zk = null;

        ZooKeeperConnection zkc = new ZooKeeperConnection(zkNodes, 2000);
        zkc.connect();
    }

    @Test
    public void testCreateFullPath() throws IOException, InterruptedException, KeeperException {
        ZooKeeperConnection zkc = new ZooKeeperConnection(zk.getZkNodes(), 30000);
        zkc.connect();
        ZooKeeperConnection.createFullPath(zkc, "/zkc/test", new byte[0], CreateMode.PERSISTENT);
        try {
            ZooKeeperConnection.createFullPath(
                    zkc, "/zkc/test", new byte[0], CreateMode.PERSISTENT);
            assertTrue(false);
        } catch (KeeperException e) {
            assertSame(e.getClass(), KeeperException.NodeExistsException.class);
        }
        ZooKeeperConnection.createFullPath(
                zkc, "/zkc/test", new byte[0], CreateMode.PERSISTENT, true);
        zkc.close();
    }

    @Test
    public void testUpdateOrCreate() throws IOException, InterruptedException, KeeperException {
        ZooKeeperConnection zkc = new ZooKeeperConnection(zk.getZkNodes(), 30000);
        zkc.connect();
        assertNull(zkc.exists("/zkc/test", false));
        ZooKeeperConnection.updateOrCreate(
                zkc, "/zkc/test", new byte[] {1, 2, 3}, CreateMode.PERSISTENT);
        assertArrayEquals(new byte[] {1, 2, 3}, zkc.getData("/zkc/test", false, new Stat()));
        ZooKeeperConnection.updateOrCreate(
                zkc, "/zkc/test", new byte[] {4, 5, 6}, CreateMode.PERSISTENT);
        assertArrayEquals(new byte[] {4, 5, 6}, zkc.getData("/zkc/test", false, new Stat()));
        zkc.close();
    }

    @Test
    public void testCreateIfNotExists() throws IOException, InterruptedException, KeeperException {
        ZooKeeperConnection zkc = new ZooKeeperConnection(zk.getZkNodes(), 30000);
        zkc.connect();
        assertNull(zkc.exists("/zkc", false));
        assertTrue(
                ZooKeeperConnection.createIfNotExists(
                        zkc, "/zkc", new byte[] {1, 2, 3}, CreateMode.PERSISTENT));
        assertFalse(
                ZooKeeperConnection.createIfNotExists(
                        zkc, "/zkc", new byte[] {4, 5, 6}, CreateMode.PERSISTENT));
        assertArrayEquals(new byte[] {1, 2, 3}, zkc.getData("/zkc", false, new Stat()));
        zkc.close();
    }

    @Test(expected = KeeperException.NoNodeException.class)
    public void testCreateIfNotExistsNoParent()
            throws IOException, InterruptedException, KeeperException {
        ZooKeeperConnection zkc = new ZooKeeperConnection(zk.getZkNodes(), 30000);
        zkc.connect();
        try {
            ZooKeeperConnection.createIfNotExists(
                    zkc, "/zkc/test", new byte[0], CreateMode.PERSISTENT);
        } finally {
            zkc.close();
        }
    }

    @Test
    public void increaseCodeCoverage() throws IOException, InterruptedException, KeeperException {
        ZooKeeperConnection zkc = new ZooKeeperConnection(zk.getZkNodes(), 30000);
        zkc.connect();
        zkc.getSessionId();
        zkc.getSessionPasswd();
        zkc.getSessionTimeout();
        zkc.register(
                new Watcher() {
                    @Override
                    public void process(WatchedEvent watchedEvent) {}
                });
        zkc.create("/zkc", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        zkc.getChildren("/zkc", false);
        zkc.getChildren("/zkc", false, new Stat());
        zkc.setData("/zkc", new byte[0], -1);
        zkc.getACL("/zkc", new Stat());
        zkc.setACL("/zkc", ZooDefs.Ids.OPEN_ACL_UNSAFE, -1);
        zkc.delete("/zkc", -1);
        zkc.close();
    }

    @Test
    public void testObjectMethods() {
        CommonMethodsTester.testObjectMethods(new ZooKeeperConnection("", 0));
    }

    @Test
    public void testGettersAndSetters() {
        CommonMethodsTester.testGettersAndSetters(
                ZooKeeperConnection.class, new ZooKeeperConnection("", 0));
    }
}
