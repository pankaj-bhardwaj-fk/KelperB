import com.flipkart.dto.ServiceNode;
import com.flipkart.healthchecks.HealthCheckI;
import com.flipkart.healthchecks.HealthStatus;
import com.flipkart.serviceprovider.ServiceProvider;
import com.flipkart.serviceprovider.ServiceProviderBuilder;
import org.apache.curator.test.TestingCluster;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created on 05/03/17 by dark magic.
 */
public class ServiceProviderTest {
    TestingCluster testingCluster;

    @Before
    public void setup() throws Exception {
        testingCluster = new TestingCluster(3);
        testingCluster.start();
        registerService("localhost-1", 9000, 1, 2);
        registerService("localhost-2", 9001, 1, 2);
        registerService("localhost-3", 9002, 1, 2);
    }

    @Test
    public void setTestingCluster() throws Exception {

    }


    public void registerService(String host, int port, final int a, int b) throws Exception {
        ServiceProviderBuilder<TestShardInfo> builder = new ServiceProviderBuilder<TestShardInfo>();
        final ServiceProvider<TestShardInfo> provider = builder.
                withConnectString(testingCluster.getConnectString()).
                withNameSpace("abcd").
                withServiceName("lalaland").
                withHealthChecks(new ArrayList<HealthCheckI>() {{
                    add(new HealthCheckI() {
                        @Override
                        public HealthStatus getStatus() {
                            return HealthStatus.HEALTHY;
                        }
                    });
                }}).
                withHealthCheckRefreshDelay(10).
                withServiceNode(new ServiceNode<TestShardInfo>("host", 10, new TestShardInfo(10, 4))).
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