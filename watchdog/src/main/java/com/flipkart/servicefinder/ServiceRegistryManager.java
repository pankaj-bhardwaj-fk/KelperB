package com.flipkart.servicefinder;

import com.flipkart.PathUtils;
import com.flipkart.dto.Mapper;
import com.flipkart.dto.ServiceNode;
import com.google.common.collect.Lists;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * mostly copied and inspired from ranger lib.
 * <p>
 * Created on 05/03/17 by dark magic.
 * <p>
 * notify on update in available nodes
 */
public class ServiceRegistryManager<T> extends Observable implements Callable<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryManager.class);

    private boolean checkForUpdate = false;
    private final Mapper<T> mapper;
    private boolean shouldContinue = true;
    private final String serviceName;

    private CuratorFramework curatorFramework = null;
    private Lock checkLock = new ReentrantLock();
    private Condition checkCondition = checkLock.newCondition();

    public ServiceRegistryManager(Mapper<T> mapper, String serviceName) {
        this.mapper = mapper;
        this.serviceName = serviceName;
    }

    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    public void start() throws Exception {
        final String parentPath = PathUtils.getPathForParentInHandShake(serviceName);
        curatorFramework.getChildren().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                switch (event.getType()) {

                    case NodeChildrenChanged: {
                        checkForUpdate();
                        break;
                    }
                    case None:
                    case NodeCreated:
                    case NodeDeleted:
                    case NodeDataChanged:
                        break;
                }
            }
        }).forPath(parentPath);
        logger.info("Started polling zookeeper for changes");
    }

    @Override
    public Boolean call() throws Exception {
        //Start checking for updates
        while (shouldContinue) {
            try {
                checkLock.lock();
                while (!checkForUpdate) {
                    checkCondition.await();
                }
                List<ServiceNode<T>> nodes = checkForUpdateOnZookeeper();
                if (null != nodes) {
                    //event send.
                    notifyObservers(nodes);
                    logger.info("Setting nodelist of size: " + nodes.size());
                } else {
                    logger.warn("No service shards/nodes found. We are disconnected from zookeeper. Keeping old list.");
                }
                checkForUpdate = false;
            } catch (Exception ex) {
                logger.error("Some error occurred while getting the child No error thrown", ex);
            } finally {
                checkLock.unlock();
            }
        }
        return true;
    }

    public void checkForUpdate() {
        try {
            checkLock.lock();
            checkForUpdate = true;
            checkCondition.signalAll();
        } finally {
            checkLock.unlock();
        }
    }

    private List<ServiceNode<T>> checkForUpdateOnZookeeper() {
        try {
            final String parentPath = PathUtils.getPathForParentInHandShake(serviceName);

            List<String> children = curatorFramework.getChildren().forPath(parentPath);
            List<ServiceNode<T>> nodes = Lists.newArrayListWithCapacity(children.size());

            for (String child : children) {
                final String path = PathUtils.getPathForChildInHandShake(serviceName, child);
                if (null == curatorFramework.checkExists().forPath(path)) {
                    continue;
                }
                byte data[] = curatorFramework.getData().forPath(path);
                if (null == data) {
                    logger.warn("Not data present for node: " + path);
                    continue;
                }
                nodes.add(mapper.deserialize(data));
            }
            return nodes;
        } catch (Exception e) {
            logger.error("Error getting service data from zookeeper: ", e);
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        shouldContinue = false;
        checkForUpdate();
        logger.debug("Stopped updater");
    }
}