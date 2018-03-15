import com.lbwwz.easyrabbitmq.TopicBroker;
import com.lbwwz.easyrabbitmq.core.SimpleRabbitAdmin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
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

//        TopicBroker topicBroker = new TopicBroker(connectionFactory);
//        topicBroker.createExchange("sm_exchange");
//        topicBroker.generateAndBindQueue("sm_q","#.wasd.#");
//
//        topicBroker.template.convertAndSend("123.wasd.123");


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

    @Test
    public void testDeclareExchange(){
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext
                ("spring/spring-rabbitmq.xml");

        SimpleRabbitAdmin simpleRabbitAdmin = applicationContext.getBean(SimpleRabbitAdmin.class);

        simpleRabbitAdmin.declareExchange(new TopicExchange("lbwwz_test_exchange",true,false));
    }

    @Test
    public void testDeclareQueue(){
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext
                ("spring/spring-rabbitmq.xml");

        SimpleRabbitAdmin simpleRabbitAdmin = applicationContext.getBean(SimpleRabbitAdmin.class);
        Queue queue = QueueBuilder.durable("lbwwz_test_queue").build();
        simpleRabbitAdmin.declareQueue(queue);
    }

    @Test
    public void testDeclareBinding(){
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext
                ("spring/spring-rabbitmq.xml");

        SimpleRabbitAdmin simpleRabbitAdmin = applicationContext.getBean(SimpleRabbitAdmin.class);
        Exchange exchange = ExchangeBuilder.topicExchange("lbwwz_test_exchange").build();
        Queue queue = QueueBuilder.durable("lbwwz_test_queue").build();
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("#.123").noargs();
        simpleRabbitAdmin.declareBinding(binding);
    }


}
