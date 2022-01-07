package com.indeed.util.zookeeper;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.common.PathUtils;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/** @author jplaisance */
public final class ZooKeeperConnection {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperConnection.class);

    private ZooKeeper zooKeeper;

    private final String zookeeperNodes;

    private final int timeout;

    public ZooKeeperConnection(String zookeeperNodes, int timeout) {
        this.zookeeperNodes = zookeeperNodes;
        this.timeout = timeout;
    }

    public void connect() throws IOException, InterruptedException {
        final AtomicReference<Watcher.Event.KeeperState> connectionState =
                new AtomicReference<Watcher.Event.KeeperState>(null);
        final Watcher watcher =
                new Watcher() {
                    @Override
                    public void process(final WatchedEvent watchedEvent) {
                        connectionState.set(watchedEvent.getState());
                    }
                };
        zooKeeper = new ZooKeeper(zookeeperNodes, timeout, watcher);
        final long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < timeout && connectionState.get() == null)
            Thread.sleep(500);
        if (connectionState.get() != Watcher.Event.KeeperState.SyncConnected) {
            zooKeeper.close();
            throw new IOException("connection failed");
        }
    }

    public boolean isConnected() {
        return zooKeeper != null && zooKeeper.getState().isAlive();
    }

    public long getSessionId() {
        return zooKeeper.getSessionId();
    }

    public byte[] getSessionPasswd() {
        return zooKeeper.getSessionPasswd();
    }

    public int getSessionTimeout() {
        return zooKeeper.getSessionTimeout();
    }

    public void addAuthInfo(final String scheme, final byte[] auth) {
        zooKeeper.addAuthInfo(scheme, auth);
    }

    public void register(final Watcher watcher) {
        zooKeeper.register(watcher);
    }

    public void close() throws InterruptedException {
        if (zooKeeper != null) zooKeeper.close();
    }

    public String create(
            final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode)
            throws KeeperException, InterruptedException {
        return zooKeeper.create(path, data, acl, createMode);
    }

    public void create(
            final String path,
            final byte[] data,
            final List<ACL> acl,
            final CreateMode createMode,
            final AsyncCallback.StringCallback cb,
            final Object ctx) {
        zooKeeper.create(path, data, acl, createMode, cb, ctx);
    }

    public void delete(final String path, final int version)
            throws InterruptedException, KeeperException {
        zooKeeper.delete(path, version);
    }

    public void delete(
            final String path,
            final int version,
            final AsyncCallback.VoidCallback cb,
            final Object ctx) {
        zooKeeper.delete(path, version, cb, ctx);
    }

    public Stat exists(final String path, final Watcher watcher)
            throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watcher);
    }

    public Stat exists(final String path, final boolean watch)
            throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watch);
    }

    public void exists(
            final String path,
            final Watcher watcher,
            final AsyncCallback.StatCallback cb,
            final Object ctx) {
        zooKeeper.exists(path, watcher, cb, ctx);
    }

    public void exists(
            final String path,
            final boolean watch,
            final AsyncCallback.StatCallback cb,
            final Object ctx) {
        zooKeeper.exists(path, watch, cb, ctx);
    }

    public byte[] getData(final String path, final Watcher watcher, final Stat stat)
            throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watcher, stat);
    }

    public byte[] getData(final String path, final boolean watch, final Stat stat)
            throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watch, stat);
    }

    public void getData(
            final String path,
            final Watcher watcher,
            final AsyncCallback.DataCallback cb,
            final Object ctx) {
        zooKeeper.getData(path, watcher, cb, ctx);
    }

    public void getData(
            final String path,
            final boolean watch,
            final AsyncCallback.DataCallback cb,
            final Object ctx) {
        zooKeeper.getData(path, watch, cb, ctx);
    }

    public Stat setData(final String path, final byte[] data, final int version)
            throws KeeperException, InterruptedException {
        return zooKeeper.setData(path, data, version);
    }

    public void setData(
            final String path,
            final byte[] data,
            final int version,
            final AsyncCallback.StatCallback cb,
            final Object ctx) {
        zooKeeper.setData(path, data, version, cb, ctx);
    }

    public List<ACL> getACL(final String path, final Stat stat)
            throws KeeperException, InterruptedException {
        return zooKeeper.getACL(path, stat);
    }

    public void getACL(
            final String path,
            final Stat stat,
            final AsyncCallback.ACLCallback cb,
            final Object ctx) {
        zooKeeper.getACL(path, stat, cb, ctx);
    }

    public Stat setACL(final String path, final List<ACL> acl, final int version)
            throws KeeperException, InterruptedException {
        return zooKeeper.setACL(path, acl, version);
    }

    public void setACL(
            final String path,
            final List<ACL> acl,
            final int version,
            final AsyncCallback.StatCallback cb,
            final Object ctx) {
        zooKeeper.setACL(path, acl, version, cb, ctx);
    }

    public List<String> getChildren(final String path, final Watcher watcher)
            throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watcher);
    }

    public List<String> getChildren(final String path, final boolean watch)
            throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watch);
    }

    public void getChildren(
            final String path,
            final Watcher watcher,
            final AsyncCallback.ChildrenCallback cb,
            final Object ctx) {
        zooKeeper.getChildren(path, watcher, cb, ctx);
    }

    public void getChildren(
            final String path,
            final boolean watch,
            final AsyncCallback.ChildrenCallback cb,
            final Object ctx) {
        zooKeeper.getChildren(path, watch, cb, ctx);
    }

    public List<String> getChildren(final String path, final Watcher watcher, final Stat stat)
            throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watcher, stat);
    }

    public List<String> getChildren(final String path, final boolean watch, final Stat stat)
            throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watch, stat);
    }

    public void getChildren(
            final String path,
            final Watcher watcher,
            final AsyncCallback.Children2Callback cb,
            final Object ctx) {
        zooKeeper.getChildren(path, watcher, cb, ctx);
    }

    public void getChildren(
            final String path,
            final boolean watch,
            final AsyncCallback.Children2Callback cb,
            final Object ctx) {
        zooKeeper.getChildren(path, watch, cb, ctx);
    }

    public void sync(final String path, final AsyncCallback.VoidCallback cb, final Object ctx) {
        zooKeeper.sync(path, cb, ctx);
    }

    public ZooKeeper.States getState() {
        return zooKeeper.getState();
    }

    public void createFullPath(String path, byte[] value, CreateMode createMode)
            throws KeeperException, InterruptedException {
        createFullPath(this, path, value, createMode);
    }

    public boolean createFullPath(
            String path, byte[] value, CreateMode createMode, boolean ignoreIfExists)
            throws KeeperException, InterruptedException {
        return createFullPath(this, path, value, createMode, ignoreIfExists);
    }

    public static void createFullPath(
            ZooKeeperConnection zooKeeperConnection,
            String path,
            byte[] value,
            CreateMode createMode)
            throws InterruptedException, KeeperException {
        createFullPath(zooKeeperConnection, path, value, createMode, false);
    }

    public static boolean createFullPath(
            ZooKeeperConnection zooKeeperConnection,
            String path,
            byte[] value,
            CreateMode createMode,
            boolean ignoreIfExists)
            throws InterruptedException, KeeperException {
        final byte[] empty = new byte[0];
        final String[] nodes = path.split("/");
        final StringBuilder pathBuilder = new StringBuilder();
        for (int i = 1; i < nodes.length - 1; i++) {
            pathBuilder.append("/").append(nodes[i]);
            createIfNotExists(
                    zooKeeperConnection, pathBuilder.toString(), empty, CreateMode.PERSISTENT);
        }
        pathBuilder.append("/").append(nodes[nodes.length - 1]);
        if (ignoreIfExists) {
            return createIfNotExists(zooKeeperConnection, path, value, createMode);
        }
        zooKeeperConnection.create(
                pathBuilder.toString(), value, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
        return true;
    }

    public boolean createIfNotExists(String path, byte[] value, CreateMode createMode)
            throws KeeperException, InterruptedException {
        return createIfNotExists(this, path, value, createMode);
    }

    public static boolean createIfNotExists(
            ZooKeeperConnection zooKeeper, String path, byte[] value, CreateMode createMode)
            throws InterruptedException, KeeperException {
        if (zooKeeper.exists(path, false) == null) {
            try {
                zooKeeper.create(path, value, ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
            } catch (KeeperException e) {
                if (e.code() != KeeperException.Code.NODEEXISTS) throw e;
                return false;
            }
            return true;
        }
        return false;
    }

    public void updateOrCreate(String path, byte[] value, CreateMode createMode)
            throws KeeperException, InterruptedException {
        updateOrCreate(this, path, value, createMode);
    }

    public static void updateOrCreate(
            ZooKeeperConnection zooKeeper, String path, byte[] value, CreateMode createMode)
            throws InterruptedException, KeeperException {
        boolean success = false;
        if (zooKeeper.exists(path, false) == null) {
            success = createFullPath(zooKeeper, path, value, createMode, true);
        }
        if (!success) zooKeeper.setData(path, value, -1);
    }

    public static String buildPath(String parent, String firstPart, String... restOfParts) {
        PathUtils.validatePath(parent);
        if (firstPart.contains("/"))
            throw new IllegalArgumentException("only parent may contain / character");
        String path = (parent.equals("/") ? parent : parent + "/") + firstPart;
        for (String part : restOfParts) {
            if (part.contains("/"))
                throw new IllegalArgumentException("only parent may contain / character");
            path = path + "/" + part;
        }
        PathUtils.validatePath(path);
        return path;
    }

    public static String getName(String path) {
        PathUtils.validatePath(path);
        if (path.equals("/")) {
            throw new IllegalArgumentException("name of / is undefined");
        }
        final int index = path.lastIndexOf('/');
        return path.substring(index + 1);
    }

    public static String getParent(String path) {
        PathUtils.validatePath(path);
        if (path.equals("/")) {
            throw new IllegalArgumentException("parent of / is undefined");
        }
        final int index = path.lastIndexOf('/');
        if (index == 0) {
            return "/";
        }
        return path.substring(0, index);
    }
}
