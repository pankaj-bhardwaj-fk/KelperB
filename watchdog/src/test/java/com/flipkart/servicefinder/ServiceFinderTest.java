package com.flipkart.servicefinder;

import org.apache.curator.test.TestingCluster;
import org.junit.Before;
import org.junit.Test;

/**
 * Created on 05/03/17 by dark magic.
 */
public class ServiceFinderTest {
    TestingCluster testingCluster;

    @Before
    public void setUp() throws Exception {
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("host-1", 9000, 1, 2);
        registerService("host-2", 9001, 1, 2);
        registerService("host-3", 9002, 1, 2);
    }

    @Test
    public void test() {

    }

    public void registerService(String host, int port, final int a, int b) throws Exception {
        ServiceFinderBuilder<TestShardInfo> builder = new ServiceFinderBuilder<TestShardInfo>();
        final ServiceFinder<TestShardInfo> provider = builder.
                withConnectString(testingCluster.getConnectString()).
                withNameSpace("abcd").
                withServiceName("lalaland").
                withHealthCheckRefreshTime(10).
                build();
        provider.start();
    }

    public static class TestShardInfo {
        private int a;
        private int b;

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public TestShardInfo(int a, int b) {

            this.a = a;
            this.b = b;
        }
    }


}