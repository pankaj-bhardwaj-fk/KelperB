package com.flipkart.dto;

/**
 * Created on 04/03/17 by dark magic.
 */
public class ServiceNode<T> {
    private final String host;
    private final int port;
    private final T data;
    private String representation;

    public String getRepresentation() {
        return host.concat(String.valueOf("---")).concat(String.valueOf(port));
    }

    public ServiceNode(String host, int port, T data) {
        this.host = host;
        this.port = port;
        this.data = data;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public T getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceNode<?> that = (ServiceNode<?>) o;

        if (port != that.port) return false;
        return host.equals(that.host);

    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }
}
