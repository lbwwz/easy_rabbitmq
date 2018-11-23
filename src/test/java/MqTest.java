import java.util.concurrent.TimeUnit;

import com.lbwwz.easyrabbitmq.AbstractBrokerManager;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author lbwwz
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-rabbitmq.xml"})
public class MqTest {


    private ConnectionFactory connectionFactory;






}
