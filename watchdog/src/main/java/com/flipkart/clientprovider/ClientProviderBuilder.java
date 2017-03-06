package com.flipkart.clientprovider;

import com.flipkart.Worker;
import com.flipkart.dto.ServiceNode;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created on 05/03/17 by dark magic.
 */
public class ClientProviderBuilder<T> {
    private String serviceName;
    private long refreshInMillis;
    private Worker worker;
    private ServiceNode<T> serviceNode;
    private CuratorFramework curatorFramework;
    private String nameSpace;
    private String connectString;

    public ClientProviderBuilder<T> withNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public ClientProviderBuilder<T> withConnectString(String connectString) {
        this.connectString = connectString;
        return this;
    }

    public ClientProviderBuilder<T> withWorker(Worker worker) {
        this.worker = worker;
        return this;
    }

    public ClientProviderBuilder<T> withRefreshInMillis(long refreshInMillis) {
        this.refreshInMillis = refreshInMillis;
        return this;
    }

    public ClientProviderBuilder<T> withCuratorFrameWork(CuratorFramework curatorFrameWork) {
        this.curatorFramework = curatorFrameWork;
        return this;
    }

    public ClientProviderBuilder<T> withServiceNode(ServiceNode<T> serviceNode) {
        this.serviceNode = serviceNode;
        return this;
    }

    public ClientProviderBuilder<T> withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public ClientProvider<T> build() {
        Preconditions.checkNotNull(nameSpace);
        Preconditions.checkNotNull(connectString);
        Preconditions.checkNotNull(serviceNode);
        Preconditions.checkNotNull(serviceName);
        Preconditions.checkNotNull(refreshInMillis);
        Preconditions.checkNotNull(worker);

        if (curatorFramework == null) {
            curatorFramework = CuratorFrameworkFactory.
                    builder().
                    namespace(nameSpace).
                    connectString(connectString).
                    retryPolicy(new ExponentialBackoffRetry(100, 100)).build();
        }

        curatorFramework.start();

        return new ClientProvider<T>(serviceNode,
                serviceName,
                refreshInMillis,
                curatorFramework,
                worker);
    }

}
