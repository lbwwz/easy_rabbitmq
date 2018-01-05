package com.lbwwz.simple_middleware;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.PropertySource;

/**
 * @author lbwwz
 */
public class SimpleBrokerService implements FactoryBean<ConnectionFactory>,BeanFactoryPostProcessor{


    private String host;
    private int port;
    private String username;
    private String password;
    private String virtualHost;
    private int channelCacheSize;
    private int channelCheckoutTimeout;
    private int connectionLimit;

    public ConnectionFactory connectionFactory;


    private ConnectionFactory buildConnection(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();

        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setChannelCacheSize(channelCacheSize);
        connectionFactory.setChannelCheckoutTimeout(200);
        connectionFactory.setConnectionLimit(2);
        return connectionFactory;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public ConnectionFactory getObject() throws Exception {
        return buildConnection();
    }

    @Override
    public Class<?> getObjectType() {
        return ConnectionFactory.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }




    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        PropertySource propertySource = ((PropertySourcesPlaceholderConfigurer)beanFactory
                .getBean("org.springframework.context.support.PropertySourcesPlaceholderConfigurer#0"))
                .getAppliedPropertySources().get("localProperties");
        //对初始数据赋值
        this.host = (String)propertySource.getProperty("rabbitmq.host");
        this.username = (String)propertySource.getProperty("rabbitmq.username");
        this.password = (String)propertySource.getProperty("rabbitmq.password");
        this.virtualHost = (String)propertySource.getProperty("rabbitmq.virtualHost");
        this.port = Integer.parseInt((String)propertySource.getProperty("rabbitmq.port"));
        this.channelCacheSize = Integer.parseInt((String)propertySource.getProperty("rabbitmq.channel.cache.size"));



    }



}
