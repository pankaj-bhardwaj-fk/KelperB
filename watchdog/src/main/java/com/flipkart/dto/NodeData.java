package com.flipkart.dto;

/**
 * Created on 03/03/17 by dark magic.
 */
public class NodeData {
    private final String host;
    private final String port;
    private final String hash;


    public NodeData(String host, String port, String hash) {
        this.host = host;
        this.port = port;
        this.hash = hash;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getHash() {
        return hash;
    }
}
