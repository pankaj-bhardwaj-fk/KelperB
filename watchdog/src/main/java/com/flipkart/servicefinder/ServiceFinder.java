package com.flipkart.servicefinder;

import com.flipkart.Hack;
import com.flipkart.PathUtils;
import com.flipkart.clientleadership.LeaderShipForClient;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 05/03/17 by dark magic.
 * <p>
 * to manage the serviceFinder
 */
@Hack(value = "Observer for leaderShipForClient ")
public class ServiceFinder<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFinder.class);

    private final ServiceRegistryManager<T> registryManager;
    private final long healthcheckRefreshTimeMillis;
    private final CuratorFramework curatorFramework;
    private final String serviceName;
    private final LeaderShipForClient<T> leaderShipForClient;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ExecutorService service = Executors.newSingleThreadExecutor();

    public ServiceFinder(String serviceName,
                         CuratorFramework curatorFramework,
                         ServiceRegistryManager<T> registryManager,
                         long healthcheckRefreshTimeMillis,
                         LeaderShipForClient<T> leaderShipForClient) {

        this.serviceName = serviceName;
        this.curatorFramework = curatorFramework;
        this.registryManager = registryManager;
        this.healthcheckRefreshTimeMillis = healthcheckRefreshTimeMillis;
        this.leaderShipForClient = leaderShipForClient;

        this.registryManager.addObserver(leaderShipForClient);
    }

    public void start() throws Exception {
        startCurator();

        registryManager.setCuratorFramework(curatorFramework);

        service.submit(registryManager);
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    registryManager.checkForUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, healthcheckRefreshTimeMillis, TimeUnit.MILLISECONDS);
    }

    private void startCurator() throws Exception {
        curatorFramework.blockUntilConnected();
        logger.debug("Connected to zookeeper cluster");
        String path = PathUtils.getPathForParentInHandShake(serviceName);
        curatorFramework.newNamespaceAwareEnsurePath(path)
                .ensure(curatorFramework.getZookeeperClient());
        logger.debug("Service Registry Started");
    }

    public void stop() {
        curatorFramework.close();
        registryManager.stop();
    }
}
