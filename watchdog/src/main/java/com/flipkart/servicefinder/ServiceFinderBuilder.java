package com.flipkart.servicefinder;

import com.flipkart.clientleadership.LeaderShipForClient;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created on 04/03/17 by dark magic.
 */
public class ServiceFinderBuilder<T> {
    private String serviceName;
    private CuratorFramework curatorFramework;
    private long healthcheckRefreshTimeMillis;
    private String nameSpace;
    private String connectString;
    private ServiceRegistryManager<T> serviceRegistryManager;
    private LeaderShipForClient<T> leaderShipForClient;

    public ServiceFinderBuilder<T> withNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public ServiceFinderBuilder<T> withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceFinderBuilder<T> withCuratorFrameWork(CuratorFramework curatorFrameWork) {
        this.curatorFramework = curatorFrameWork;
        return this;
    }

    public ServiceFinderBuilder<T> withHealthCheckRefreshTime(int healthCheckRefreshTime) {
        this.healthcheckRefreshTimeMillis = healthCheckRefreshTime;
        return this;
    }

    public ServiceFinderBuilder<T> withConnectString(String connectString) {
        this.connectString = connectString;
        return this;
    }

    public ServiceFinderBuilder<T> withServiceRegistryManager(ServiceRegistryManager<T> serviceRegistryManager) {
        this.serviceRegistryManager = serviceRegistryManager;
        return this;
    }

    public ServiceFinderBuilder<T> withLeaderShipForClient(LeaderShipForClient<T> leaderShipForClient) {
        this.leaderShipForClient = leaderShipForClient;
        return this;
    }

    public ServiceFinder<T> build() {
        Preconditions.checkNotNull(serviceRegistryManager);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(leaderShipForClient);
        Preconditions.checkNotNull(healthcheckRefreshTimeMillis);
        Preconditions.checkNotNull(connectString);
        Preconditions.checkNotNull(nameSpace);
        Preconditions.checkNotNull(leaderShipForClient);

        if (curatorFramework == null) {
            curatorFramework = CuratorFrameworkFactory.
                    builder().
                    connectString(connectString).
                    namespace(nameSpace).
                    retryPolicy(new ExponentialBackoffRetry(100, 100)).
                    build();
        }
        curatorFramework.start();

        return new ServiceFinder<T>(serviceName,
                curatorFramework,
                serviceRegistryManager,
                healthcheckRefreshTimeMillis,
                leaderShipForClient);
    }
}
