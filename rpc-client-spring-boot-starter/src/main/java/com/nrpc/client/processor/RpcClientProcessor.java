package com.nrpc.client.processor;

import com.nrpc.client.annotation.RpcAutowired;
import com.nrpc.client.config.RpcClientProperties;
import com.nrpc.client.proxy.ClientProxyFactory;
import com.nrpc.discovery.DiscoveryService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 处理带有Rpc自动注入注解的字段，
 * 并将其注入到对应的bean中
 * * @Classname RpcClientProcessor
 * * @Description bean 后置处理器 获取所有bean
 * * 判断bean字段是否被 {@link com.nrpc.client.annotation.RpcAutowired } 注解修饰
 * * 动态修改被修饰字段的值为代理对象 {@link ClientProxyFactory}
 * * BeanFactoryPostProcessor 接口允许定制容器行为，可以在容器实例化 Bean 之前对其进行修改。
 * * 实现该接口需要实现 postProcessBeanFactory 方法，在该方法中可以对 BeanFactory 进行操作。
 * * ApplicationContextAware 接口用于获取应用程序上下文，实现该接口需要实现 setApplicationContext 方法，
 * * 通过该方法可以获取到 ApplicationContext 对象并进行相应操作。
 */
public class RpcClientProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {
    private ClientProxyFactory clientProxyFactory;

    private DiscoveryService discoveryService;

    private RpcClientProperties properties;

    private ApplicationContext applicationContext;

    public RpcClientProcessor(ClientProxyFactory clientProxyFactory, DiscoveryService discoveryService, RpcClientProperties properties) {
        this.clientProxyFactory = clientProxyFactory;
        this.discoveryService = discoveryService;
        this.properties = properties;
    }

    /**
     * 容器实例化 Bean 之前对其进行修改
     *
     * @param beanFactory
     * @throws BeansException
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (String beanDefinitionName : beanFactory.getBeanDefinitionNames()) {
            //获取bean的定义
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            //获取bean的类型
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName != null) {
                // 根据 bean 的类名解析出对应的 Class 对象
                Class<?> clazz = ClassUtils.resolveClassName(beanClassName, this.getClass().getClassLoader());
                // 然后通过 ReflectionUtils.doWithFields 方法遍历该类的所有字段，对于带有 @RpcAutowired 注解的字段
                ReflectionUtils.doWithFields(clazz, field -> {
                    // 带有 @RpcAutowired 注解的字段，为其创建代理对象并设置到字段中去
                    RpcAutowired rpcAutowired = AnnotationUtils.getAnnotation(field, RpcAutowired.class);
                    if (rpcAutowired != null) {
                        // 从应用上下文中获取 bean 实例
                        Object bean = applicationContext.getBean(clazz);
                        // 设置字段可访问，即使 private 字段也可以进行访问
                        field.setAccessible(true);
                        // 修改为代理对象
                        ReflectionUtils
                                .setField(
                                        field,// 要设置的字段
                                        bean,//  字段所属的 bean 实例,要设置字段的目标对象（静态字段为 null）
                                        // 创建代理对象，并设置到字段中
                                        clientProxyFactory
                                                .getProxy(field.getType(), rpcAutowired.version(), discoveryService, properties));
                    }
                });
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
