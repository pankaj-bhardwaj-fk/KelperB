package com.flipkart.nodeselector;

import com.flipkart.dto.ServiceNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created on 04/03/17 by dark magic.
 */
public class RandomNodeSelector<T> implements NodeSelectorI<T> {
    private final Random random;

    public RandomNodeSelector() {
        random = new Random();
    }

    public List<ServiceNode<T>> getNode(final List<ServiceNode<T>> serviceNodes) {
        final int size = serviceNodes.size();
        return new ArrayList<ServiceNode<T>>() {{
            add(serviceNodes.get(random.nextInt(size)));
        }};
    }
}
