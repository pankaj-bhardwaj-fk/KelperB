package com.flipkart.serviceprovider;

import com.flipkart.PathUtils;
import com.flipkart.dto.Mapper;
import com.flipkart.dto.ServiceNode;
import com.flipkart.healthchecks.HealthCheckI;
import com.flipkart.healthchecks.HealthStatus;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * inspired with ranger lib.
 */

/**
 * Created on 03/03/17 by dark magic.
 * <p>
 * <p>
 * SRP:- only to consistently update itself as provider of data node.
 */

public class ServiceProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProvider.class);

    private final String serviceName;
    private final CuratorFramework curatorFramework;
    private final long healthcheckRefreshTimeMillis;
    private final List<HealthCheckI> healthChecks;
    private final ServiceNode<T> serviceNode;
    private final AtomicBoolean nodeProvided = new AtomicBoolean(false);
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Mapper<T> mapper;

    public ServiceProvider(String serviceName,
                           CuratorFramework curatorFramework,
                           long healthcheckRefreshTimeMillis,
                           List<HealthCheckI> healthChecks,
                           ServiceNode<T> serviceNode, Mapper<T> mapper) {

        this.serviceName = serviceName;
        this.curatorFramework = curatorFramework;
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        this.healthChecks = healthChecks;
        this.serviceNode = serviceNode;
        this.mapper = mapper;
    }

    public void start() throws Exception {
        curatorFramework.blockUntilConnected();
        String path = PathUtils.getPathForParentInHandShake(serviceName);
        curatorFramework.newNamespaceAwareEnsurePath(path).
                ensure(curatorFramework.getZookeeperClient());
        logger.debug("Connected to zookeeper");
        executorService.scheduleWithFixedDelay(new HealthChecker(healthChecks),
                0, healthcheckRefreshTimeMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * zookeeper takes care of namespace itself.
     * path --> in provider as /namespace/servicename/hostname
     * aka  --> /namespace/shard-x/host---port
     *
     * @throws Exception
     */
    private void updateStatus() throws Exception {
        final String path = PathUtils.getPathForChildInHandShake(serviceName, serviceNode.getRepresentation());
        if (curatorFramework.checkExists().forPath(path) == null) {
            createPath();
        }
        curatorFramework.setData().forPath(path, mapper.serializer(serviceNode).getBytes());
        nodeProvided.set(true);
    }

    public boolean isServiceAvailable() {
        return nodeProvided.get();
    }

    private void createPath() throws Exception {
        final String path = PathUtils.getPathForChildInHandShake(serviceName, serviceNode.getRepresentation());
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(path,
                mapper.serializer(serviceNode).getBytes());
    }

    private void deletePath() throws Exception {
        final String path = PathUtils.getPathForChildInHandShake(serviceName, serviceNode.getRepresentation());
        curatorFramework.delete().forPath(path);
        nodeProvided.set(false);
    }

    public void stop() {
        curatorFramework.close();
        executorService.shutdown();
    }

    /**
     * why private inner class:-
     * because it manipulates the state of the object and it's composition does not make sense in the original class
     * in such case we can have a seperate class but calling it private limits it's scope to this one only.
     * <p>
     * If further healthCheck is needed we can move it away.
     */
    private class HealthChecker implements Runnable {
        private final List<HealthCheckI> healthCheckIList;

        public HealthChecker(List<HealthCheckI> healthCheckIList) {
            this.healthCheckIList = healthCheckIList;
        }

        public void run() {
            HealthStatus status = HealthStatus.HEALTHY;
            for (HealthCheckI healthCheckI : healthCheckIList) {
                if (healthCheckI.getStatus() == HealthStatus.UNHEALTHY)
                    status = HealthStatus.UNHEALTHY;
            }
            if (status == HealthStatus.HEALTHY) {
                try {
                    updateStatus();
                } catch (Exception e) {
                    logger.error("Error in updateStatus {}", e);
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    logger.info("Deleteing path for serviceNode host {} port {}", serviceNode.getHost(), serviceNode.getPort());
                    deletePath();
                } catch (Exception e) {
                    logger.error("Error in deletePath ", e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
