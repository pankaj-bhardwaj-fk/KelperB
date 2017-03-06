package com.flipkart.nodeselector;

import com.flipkart.dto.ServiceNode;

import java.util.List;

/**
 * Created on 04/03/17 by dark magic.
 */
public interface NodeSelectorI<T> {
    List<ServiceNode<T>> getNode(List<ServiceNode<T>> nodes);
}
