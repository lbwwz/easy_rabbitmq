import com.lbwwz.easyrabbitmq.TopicBroker;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.TimeUnit;

/**
 * @author lbwwz
 */

@RunWith(SpringJUnit4ClassRunner.class)

@ContextConfiguration(locations = {"classpath:spring/spring-rabbitmq.xml"})
public class MqTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    public void test1() throws InterruptedException {

        TopicBroker topicBroker = new TopicBroker(connectionFactory);
        topicBroker.createBroker("sm_exchange");
        topicBroker.generateAndBindQueue("sm_q","#.wasd.#");

        topicBroker.template.convertAndSend("123.wasd.123");


        TimeUnit.MINUTES.sleep(1L);
    }



    @Test
    public void test2(){
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setExchange("sm_exchange");

        TopicExchange exchange  = new TopicExchange("sm_excahnge",true,false);
        Queue queue = new Queue("sm_queue",true);
        Binding bind = new Binding("sm_queue", Binding.DestinationType.QUEUE,"sm_exchange","#.msg",null);
        template.convertAndSend("123.msg","asdasdasdasdasdasd");
    }


}
