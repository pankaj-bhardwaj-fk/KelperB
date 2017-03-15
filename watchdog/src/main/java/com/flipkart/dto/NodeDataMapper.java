package com.flipkart.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created on 14/03/17 by dark magic.
 */
//TODO making hard assumption for generic type;
public class NodeDataMapper<T> implements Mapper<T> {
    private ObjectMapper mapper = new ObjectMapper();

    public NodeDataMapper() {
    }

    @Override
    public String serializer(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Obj not serialized ", e);
        }
    }

    @Override
    public ServiceNode<T> deserialize(byte[] bytes) {
        TypeReference<ServiceNode<NodeData>> typeReference = new TypeReference<ServiceNode<NodeData>>() {
        };
        try {
            return mapper.readValue(bytes, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Error in deserialization ", e);
        }
    }
}
