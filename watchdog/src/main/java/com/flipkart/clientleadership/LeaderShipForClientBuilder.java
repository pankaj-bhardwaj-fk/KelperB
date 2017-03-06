package com.flipkart.clientleadership;

import com.flipkart.Worker;
import com.flipkart.dto.Mapper;
import com.flipkart.dto.ServiceNode;
import com.flipkart.nodeselector.NodeSelectorI;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created on 05/03/17 by dark magic.
 */
public class LeaderShipForClientBuilder<T> {
    private Mapper<T> mapper;
    private String serviceName;
    private Worker worker;
    private NodeSelectorI<T> nodeSelectorI;
    private ServiceNode<T> selfData;
    private String nameSpace;
    private String connectString;
    private CuratorFramework curatorFrameWork;

    public LeaderShipForClientBuilder<T> withMapper(Mapper<T> mapper) {
        this.mapper = mapper;
        return this;
    }

    public LeaderShipForClientBuilder<T> withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public LeaderShipForClientBuilder<T> withWorker(Worker worker) {
        this.worker = worker;
        return this;
    }

    public LeaderShipForClientBuilder<T> withNodeSelector(NodeSelectorI<T> nodeSelector) {
        this.nodeSelectorI = nodeSelector;
        return this;
    }

    public LeaderShipForClientBuilder<T> withSelfData(ServiceNode<T> selfData) {
        this.selfData = selfData;
        return this;
    }

    public LeaderShipForClientBuilder<T> withNameSpaceString(String nameSpace) {
        this.nameSpace = nameSpace;
        return this;
    }

    public LeaderShipForClientBuilder<T> withConnectString(String connectString) {
        this.connectString = connectString;
        return this;
    }

    public LeaderShipForClientBuilder<T> withCuratorFrameWork(CuratorFramework curatorFrameWork) {
        this.curatorFrameWork = curatorFrameWork;
        return this;
    }

    public LeaderShipForClient<T> build() {
        if (curatorFrameWork == null) {
            curatorFrameWork = CuratorFrameworkFactory.
                    builder().
                    namespace(nameSpace).
                    connectString(connectString).
                    retryPolicy(new ExponentialBackoffRetry(100, 100)).
                    build();
        }
        curatorFrameWork.start();
        return new LeaderShipForClient<T>(serviceName,
                selfData,
                worker,
                nodeSelectorI,
                mapper,
                curatorFrameWork);
    }


}
