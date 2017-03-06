package com.flipkart.clientprovider;

import com.flipkart.Worker;
import com.flipkart.dto.Result;
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

            String path = null;
            try {
                if (curatorFramework.checkExists().forPath(path) != null) {
                    update();
                }
            } catch (Exception e) {
                logger.error("Something went wrong in your leaderShipClass ", e);
            }
        }

        private void update() throws Exception {
            String path = String.format("/%s/%s", serviceName, serviceNode.getRepresentation());
            List<String> children = curatorFramework.getChildren().forPath(path);
            String shortestChild = getShortestChild(children);
            if (shortestChild != null && previousChild != shortestChild) {
                // new child in the town.
                path = String.format("/%s/%s/%s", serviceName, serviceNode.getRepresentation(), shortestChild);
                byte[] data = curatorFramework.getData().forPath(path);
                previousChild = shortestChild;
                logger.info("Get the child leader with {}", new String(data));
                // start tcp server port.
                worker.setData();
                Result result = worker.doWork();
                //TODO no work as server has to be HA.
                if (result == Result.SUCCESSFUL) {
                    // do nothing.
                    logger.info("Successful as a server for host {} port {}", );
                } else {
                    logger.error("Failed as a server for host {} and port {} and result Type", , result);
                    //
                }
            } else {
                logger.error("Something went wrong {}", shortestChild);
            }
        }

        //TODO get shortest child based on child lexography.
        private String getShortestChild(List<String> children) {
            if (!children.isEmpty())
                return children.get(0);
            return null;
        }
    }
}
