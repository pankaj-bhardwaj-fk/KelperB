package com.flipkart.clientleadership;

import com.flipkart.Hack;
import com.flipkart.PathUtils;
import com.flipkart.Worker;
import com.flipkart.dto.Mapper;
import com.flipkart.dto.ResultType;
import com.flipkart.dto.ServiceNode;
import com.flipkart.nodeselector.NodeSelectorI;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;

/**
 * Created on 04/03/17 by dark magic.
 * <p>
 * client here is in server/client arch. so receiving end.
 * <p>
 * :- find the leaderShip on service finder sidezx.
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
    }

    public void start() {
        this.executorService.submit(new FindLeaderShip());
    }

    @Override
    public void update(Observable o, Object arg) {
        nodes((List<ServiceNode<T>>) arg);
    }

    private void nodes(List<ServiceNode<T>> serviceNodes) {
        this.serviceNodes = serviceNodes;
    }

    /**
     * it's work is to only find the leaderShip and disable the previous one if it wasn't completed.
     */
    private class FindLeaderShip implements Runnable {
        private ExecutorService workerThread = Executors.newSingleThreadExecutor();
        private Future<Boolean> doesWorkerFinishes;
        @Override
        public void run() {
            try {
                while (shouldContinue()) {

                    if (serviceNodes != null) {
                        if (currentProviderNode == null || !serviceNodeExist(currentProviderNode, serviceNodes)) {
                            ServiceNode<T> newLeaderNode = nodeSelectorI.getNode(serviceNodes).get(0);
                            destroyPreviousWorkerThread();
                            createPathForLeaderShip(serviceName, newLeaderNode);

                            if (doesGotLeaderShip(newLeaderNode)) {
                                //can be sync thread now what ever you want to do
                                currentProviderNode = newLeaderNode;

                                startNewWorkerThread(newLeaderNode);
                            }
                        }
                    } else {
                        Thread.sleep(100);
                    }
                }
            } catch (Exception ex) {
                logger.error("Some Error occurred ", ex);
            }
        }

        private boolean shouldContinue() throws ExecutionException, InterruptedException {
            if (doesWorkerFinishes != null && doesWorkerFinishes.isDone()) {
                if (doesWorkerFinishes.get() == true) return false;
            }
            return true;
        }

        private void startNewWorkerThread(ServiceNode<T> leaderNode) {
            worker.setData(leaderNode);
            doesWorkerFinishes = workerThread.submit(new WorkerThread(worker));
        }

        private void destroyPreviousWorkerThread() throws InterruptedException {
            if (!workerThread.isShutdown()) {
                workerThread.shutdownNow();
                workerThread.awaitTermination(10, TimeUnit.MILLISECONDS);
            }
        }
    }

    private class WorkerThread implements Callable<Boolean> {
        private final Worker worker;

        public WorkerThread(Worker worker) {
            this.worker = worker;
        }

        @Override
        public Boolean call() {
            try {
                ResultType resultType = worker.doWork();
                if (resultType == ResultType.SUCCESSFUL) {
                    logger.info("SuccessFul execution done here!!");
                    logger.info("We are done here!!");
                    return true;
                } else if (resultType == ResultType.FAIL_NO_RETRY) {
                    logger.error("Failed but no retry ");
                    return true;
                } else {
                    logger.error("Failed but retry!!");
                    return false;
                }
            } catch (Exception ex) {
                logger.error("Exception occurred ", ex);
                throw new RuntimeException(ex);
            } finally {
                worker.releaseResources();
            }
        }
    }

    private void createPathForLeaderShip(String serviceName, ServiceNode<T> newLeader) throws Exception {
        String path = PathUtils.getPathForChildInLeaderShipElection(serviceName, String.valueOf(newLeader.hashCode()));
        if (curatorFramework.checkExists().forPath(path) == null) {
            createPath(path, newLeader);
        }
        logger.info("Created the path checking for leaderShip!!");
    }

    private void createPath(String path, ServiceNode<T> newLeader) throws Exception {
        curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(path,
                mapper.serializer(newLeader).getBytes());
    }

    private boolean doesGotLeaderShip(ServiceNode<T> newLeaderNode) throws Exception {
        byte[] leaderData = mapper.serializer(newLeaderNode).getBytes();
        String path = PathUtils.getPathForParentInLeaderShipElection(serviceName);
        List<String> children = curatorFramework.getChildren().forPath(path);
        logger.info("Child for the current node {}", children.toArray());
        for (String child : children) {
            String childPath = PathUtils.getPathForChildInLeaderShipElection(serviceName, child);
            byte[] data = curatorFramework.getData().forPath(childPath);
            if (byteDataEquals(data, leaderData)) {
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
        return serviceNodes.contains(leaderShipNode);
    }

    public void stop() {
        curatorFramework.close();
        executorService.shutdownNow();
    }
}
