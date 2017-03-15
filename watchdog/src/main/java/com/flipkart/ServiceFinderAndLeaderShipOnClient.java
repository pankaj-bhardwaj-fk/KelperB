package com.flipkart;

import com.flipkart.clientleadership.LeaderShipForClient;
import com.flipkart.servicefinder.ServiceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 14/03/17 by dark magic.
 */
public class ServiceFinderAndLeaderShipOnClient<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFinderAndLeaderShipOnClient.class);
    private final ServiceFinder<T> serviceFinder;
    private final LeaderShipForClient<T> leaderShipForClient;

    public ServiceFinderAndLeaderShipOnClient(ServiceFinder<T> serviceFinder,
                                              LeaderShipForClient<T> leaderShipForClient) {
        this.serviceFinder = serviceFinder;
        this.leaderShipForClient = leaderShipForClient;
    }

    public void start() {
        try {
            serviceFinder.start();
        } catch (Exception e) {
            logger.error("Service Finder failed to start !!", e);
        }
    }

    public void stop() {
        serviceFinder.stop();
        leaderShipForClient.stop();
    }
}
