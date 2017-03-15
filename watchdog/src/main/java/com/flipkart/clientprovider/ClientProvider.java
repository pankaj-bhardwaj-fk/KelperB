package com.flipkart.clientprovider;

import com.flipkart.PathUtils;
import com.flipkart.Worker;
import com.flipkart.dto.ResultType;
import com.flipkart.dto.ServiceNode;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created on 04/03/17 by dark magic.
 * <p>
 * providing the (leader node) opted for current provider. aka
 * <p>
 * the one which is suppose to be getting the connection
 * <p>
 * this id decoupled with the node provider from it's original host.
 *
 *
 * these are provider's node for final leaderElection.
 *
 * path
 * /namespace/service-clientprovider/hash
 *
 * eg:- /namespace/shard-x-clientprovider/hash
 */
public class ClientProvider<T> {
    private static final Logger logger = LoggerFactory.getLogger(ClientProvider.class);
    private final ServiceNode<T> serviceNode;
    private final String serviceName;
    private final long refreshInMillis;
    private final CuratorFramework curatorFramework;
    private String previousChild = null;
    private final Worker worker;
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public ClientProvider(ServiceNode<T> serviceNode,
                          String serviceName,
                          long refreshInMillis,
                          CuratorFramework curatorFramework,
                          Worker worker) {
        this.serviceNode = serviceNode;
        this.serviceName = serviceName;
        this.refreshInMillis = refreshInMillis;
        this.curatorFramework = curatorFramework;
        this.worker = worker;
    }

    public void start() throws Exception {
        curatorFramework.blockUntilConnected();
        logger.info("Node connected ");
        service.scheduleWithFixedDelay(new LeaderShipForSelfNodeForProvider(),
                0, refreshInMillis, TimeUnit.MILLISECONDS);
    }

    private class LeaderShipForSelfNodeForProvider implements Runnable {

        @Override
        public void run() {
            String path = PathUtils.getPathForParentInLeaderShipElection(serviceName);
            try {
                if (curatorFramework.checkExists().forPath(path) != null) {
                    update(path);
                }
            } catch (Exception e) {
                logger.error("Something went wrong in your leaderShipClass ", e);
            }
        }

        private void update(String path) throws Exception {
            List<String> children = curatorFramework.getChildren().forPath(path);
            String leaderNode = getLeaderNode(children);
            if (leaderNode != null && previousChild != leaderNode) {
                // new child in the town.
                path = PathUtils.getPathForChildInLeaderShipElection(serviceName, String.valueOf(serviceNode.hashCode()));
                byte[] data = curatorFramework.getData().forPath(path);
                previousChild = leaderNode;
                logger.info("Get the child leader with {}", new String(data));
                // createPathForLeaderShip underlying worker(in my case tcp ).
                worker.setData(leaderNode);
                ResultType resultType = worker.doWork();
                if (resultType == ResultType.SUCCESSFUL) {
                    logger.info("Successful as a server for host {} port {}",
                            serviceNode.getHost(), serviceNode.getPort());
                } else {
                    logger.error("Failed as a server for host {} and port {} and result Type {}",
                            serviceNode.getHost(), serviceNode.getPort(), resultType);
                }
            } else {
                logger.error("Something went wrong {}", leaderNode);
            }
        }

        /**
         * sequential nodes are of named string type with value of an integer.
         *
         * @param nodes
         * @return
         */
        private String getLeaderNode(List<String> nodes) {
            if (!nodes.isEmpty()) {
                String leaderNode = null;
                for (String node : nodes) {
                    if (node == null) leaderNode = node;
                    else {
                        int val = Integer.parseInt(node);
                        int leaderValue = Integer.parseInt(leaderNode);
                        if (val < leaderValue) {
                            leaderNode = node;
                        }
                    }
                }
                return leaderNode;
            }
            return null;
        }
    }

    public void stop() {
        curatorFramework.close();
        service.shutdown();
    }
}
