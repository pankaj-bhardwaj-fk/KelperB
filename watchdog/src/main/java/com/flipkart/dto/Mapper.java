package com.flipkart.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created on 04/03/17 by dark magic.
 */
public class Mapper<T> {
    protected final ObjectMapper mapper = new ObjectMapper();

    public Mapper() {
    }

    public String serializer(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public ServiceNode<T> deserialize(byte[] bytes) throws IOException {
        return mapper.readValue(bytes, new TypeReference<ServiceNode<NodeData>>() {
        });
    }
}
