package com.flipkart.clientleadership;

import com.flipkart.Hack;
import com.flipkart.Worker;
import com.flipkart.dto.Mapper;
import com.flipkart.dto.Result;
import com.flipkart.dto.ServiceNode;
import com.flipkart.nodeselector.NodeSelectorI;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created on 04/03/17 by dark magic.
 * <p>
 * client here is in server/client arch. so receiving end.
 * <p>
 * :- find the leaderShip on service provider nodes.
 */
//TODO path
//TODO register as observer in ServiceRegistryManager
@Hack(value = "observer pattern for leaderShipForClient")

public class LeaderShipForClient<T> implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(LeaderShipForClient.class);
    private final NodeSelectorI<T> nodeSelectorI;
    private final String serviceName;
    private final ServiceNode<T> selfData;
    private List<ServiceNode<T>> serviceNodes;
    private final Worker worker;
    private final Mapper mapper;
    private ServiceNode<T> currentProviderNode;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final CuratorFramework curatorFramework;

    public LeaderShipForClient(String serviceName,
                               ServiceNode<T> selfData,
                               Worker worker,
                               NodeSelectorI<T> nodeSelectorI,
                               Mapper mapper, CuratorFramework curatorFramework) {
        this.nodeSelectorI = nodeSelectorI;
        this.serviceName = serviceName;
        this.selfData = selfData;
        this.worker = worker;
        this.mapper = mapper;
        this.curatorFramework = curatorFramework;
        executorService.submit(new FindLeaderShip());
    }

    @Override
    public void update(Observable o, Object arg) {
        nodes((List<ServiceNode<T>>) arg);
    }

    public void nodes(List<ServiceNode<T>> serviceNodes) {
        this.serviceNodes = serviceNodes;
    }

    private class FindLeaderShip implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    if (serviceNodes != null) {
                        if (currentProviderNode == null || !serviceNodeExist(currentProviderNode, serviceNodes)) {
                            ServiceNode<T> currentExpectedProviderNode = nodeSelectorI.getNode(serviceNodes).get(0);

                            start(serviceName, currentExpectedProviderNode, selfData);

                            if (doesGotLeaderShip(mapper.serializer(selfData).getBytes(), currentExpectedProviderNode)) {
                                //can be sync thread now what ever you want to do
                                currentProviderNode = currentExpectedProviderNode;
                                Result resultType = worker.doWork();
                                if (resultType == Result.SUCCESSFUL) {
                                    //yo completed.
                                    logger.info("SuccessFul execution done here!!");
                                    logger.info("We are done here!!");
                                    return;
                                } else if (resultType == Result.FAIL_NO_RETRY) {
                                    // do nothing just retry.
                                    logger.error("Failed but no retry ");
                                    return;
                                } else {
                                    // do nothing.
                                    logger.error("Failed but retry!!");
                                }
                            }
                        }
                    } else {
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    logger.error("Some Error occurred ", ex);
                }
            }
        }

        public void start(String serviceName, ServiceNode<T> node, ServiceNode<T> selfNode) throws Exception {
            //TODO have to look for the path
            String path = String.format("/%s/%s", serviceName, node.getRepresentation());
            if (curatorFramework.checkExists().forPath(path) == null) {
                createPath(path, selfNode);
            }
            logger.info("Created the path checking for leaderShip!!");
        }

        private void createPath(String path, ServiceNode<T> selfData) throws Exception {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path,
                    mapper.serializer(selfData).getBytes());
        }

        private boolean doesGotLeaderShip(byte[] selfData, ServiceNode<T> node) throws Exception {
            //TODO path
            String path = String.format("/%s/%s", serviceName, node.getRepresentation());
            List<String> children = curatorFramework.getChildren().forPath(path);
            for (String child : children) {
                //TODO path
                String childPath = null;
                byte[] data = curatorFramework.getData().forPath(childPath);
                if (byteDataEquals(data, selfData)) {
                    return true;
                }
            }
            return false;
        }

        private boolean byteDataEquals(byte[] data, byte[] selfData) {
            if (data.length != selfData.length) return false;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != selfData[i]) return false;
            }
            return true;
        }

        private boolean serviceNodeExist(ServiceNode<T> leaderShipNode, List<ServiceNode<T>> serviceNodes) {
            for (ServiceNode<T> node : serviceNodes) {
                if (node.equals(leaderShipNode)) {
                    return true;
                }
            }
            return false;
        }
    }
}
