package com.flipkart.serviceprovider;

import com.flipkart.dto.Mapper;
import com.flipkart.dto.ServiceNode;
import com.flipkart.healthchecks.HealthCheckI;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;

/**
 * Created on 04/03/17 by dark magic.
 * <p>
 * Builder pattern for object creation
 */
public class ServiceProviderBuilder<T> {
    private String nameSpace;
    private String serviceName;
    private CuratorFramework curatorFramework;
    private String connectionString;
    private long healthcheckRefreshTimeMillis;
    private ServiceNode<T> serviceNode;
    private List<HealthCheckI> healthCheck;
    private Mapper<T> mapper;

    public ServiceProviderBuilder<T> withNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public ServiceProviderBuilder<T> withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ServiceProviderBuilder<T> withConnectString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public ServiceProviderBuilder<T> withHealthCheckRefreshDelay(long healthCheckRefreshDelay) {
        this.healthcheckRefreshTimeMillis = healthCheckRefreshDelay;
        return this;
    }

    public ServiceProviderBuilder<T> withCuratorFrameWork(CuratorFramework curatorFrameWork) {
        this.curatorFramework = curatorFrameWork;
        return this;
    }

    public ServiceProviderBuilder<T> withHealthChecks(List<HealthCheckI> healthChecks) {
        this.healthCheck = healthChecks;
        return this;
    }

    public ServiceProviderBuilder<T> withServiceNode(ServiceNode<T> serviceNode) {
        this.serviceNode = serviceNode;
        return this;
    }

    public ServiceProviderBuilder<T> withSerializer(Mapper<T> mapper) {
        this.mapper = mapper;
        return this;
    }

    public ServiceProvider<T> build() {
        Preconditions.checkNotNull(nameSpace);
        Preconditions.checkNotNull(connectionString);
        if (curatorFramework == null) {
            curatorFramework = CuratorFrameworkFactory.
                    builder().
                    namespace(nameSpace).
                    connectString(connectionString).
                    retryPolicy(new ExponentialBackoffRetry(100, 100))
                    .build();
            curatorFramework.start();
        }

        return new ServiceProvider<T>(nameSpace,
                curatorFramework,
                healthcheckRefreshTimeMillis,
                healthCheck,
                serviceNode,
                mapper);
    }
}