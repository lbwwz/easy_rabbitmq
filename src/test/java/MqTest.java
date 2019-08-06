import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.lbwwz.easyrabbitmq.AbstractBrokerManager;
import com.lbwwz.easyrabbitmq.AbstractBrokerMessageProcessImpl;
import com.lbwwz.easyrabbitmq.BrokerMessageProcess;
import com.lbwwz.easyrabbitmq.DefaultBrokerMessageProcessImpl;
import com.lbwwz.easyrabbitmq.QueueService;
import com.lbwwz.easyrabbitmq.cache.MqConnectionFactory;
import com.lbwwz.easyrabbitmq.util.Constant;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author lbwwz
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-rabbitmq.xml"})
public class MqTest {

    @Autowired
    private QueueService queueService;

    @Test
    public void consume() throws InterruptedException {
        queueService.consume("test_msg", null, null, 1, String.class, System.out::println);
        Thread.sleep(1000000);
    }

    @Autowired
    private BrokerMessageProcess defaultBrokerMessageProcess;

    @Test
    public void provide() throws InterruptedException {
        defaultBrokerMessageProcess.publish("test_msg", BuiltinExchangeType.TOPIC.name(), Constant.DEFAULT_ROUTING_KEY, 5000L, "hehehe");

        Thread.sleep(1000000);
    }

    @Test
    public void test1() throws IOException, TimeoutException {
        Connection connection = MqConnectionFactory.getInstance().getConnection();

        Channel channel = connection.createChannel();
        channel.queueBind("_queue_test_msg", "_exchange_test_msg", Constant.DEFAULT_ROUTING_KEY,
            null);
    }

}
