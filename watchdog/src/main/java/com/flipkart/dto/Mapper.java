package com.flipkart.dto;

/**
 * Created on 04/03/17 by dark magic.
 */
public interface Mapper<T> {

    //    public Mapper() {
//    }
    public String serializer(Object obj);

    public ServiceNode<T> deserialize(byte[] bytes);
//    public String serializer(Object obj) throws JsonProcessingException {
//        return mapper.writeValueAsString(obj);
//    }
//
//    public ServiceNode<T> deserialize(byte[] bytes) throws IOException {
//        return mapper.readValue(bytes, new TypeReference<ServiceNode<NodeData>>() {
//        });
//    }
}
