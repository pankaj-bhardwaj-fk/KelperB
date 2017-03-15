package com.flipkart;

import com.flipkart.clientprovider.ClientProvider;
import com.flipkart.serviceprovider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created on 14/03/17 by dark magic.
 */
public class ServiceProviderAndLeaderShipElectionForClient<T> {
    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderAndLeaderShipElectionForClient.class);

    private final ServiceProvider<T> serviceProvider;
    private final ClientProvider<T> clientProvider;

    public ServiceProviderAndLeaderShipElectionForClient(ServiceProvider<T> serviceProvider,
                                                         ClientProvider<T> clientProvider) {
        this.serviceProvider = serviceProvider;
        this.clientProvider = clientProvider;
    }

    public void start() {
        try {
            serviceProvider.start();
            clientProvider.start();
        } catch (Exception e) {
            logger.error("ServiceProvider failed to start!!!");
        }
    }

    public void stop() {
        serviceProvider.stop();

    }
}
