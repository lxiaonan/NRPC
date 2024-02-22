package com.nrpc.register;

import com.nrpc.common.ServiceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
@Slf4j
public class ZooKeeperRegisterServiceImpl implements RegisterService{
    private final int BASE_SLEEP_TIME_MS = 1000;
    private final int MAX_RETRIES = 3;
    private final String ZK_BASE_PATH = "/nrpc";
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;

    public ZooKeeperRegisterServiceImpl(String address) {
        try{
            // 建立zk连接
            CuratorFramework client = CuratorFrameworkFactory
                    .newClient(address, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
            client.start();
            /**
             * ServiceInfo.class：指定了服务实例的类型为 ServiceInfo。
             * client：指定了与 Zookeeper 通信的 CuratorFramework 客户端。
             * serializer：指定了上面创建的 JsonInstanceSerializer 对象，用于序列化和反序列化服务实例信息。
             * basePath(ZK_BASE_PATH)：指定了在 Zookeeper 上用于存储服务实例信息的基本路径。
             */
            //将 ServiceInfo 对象序列化为 JSON 格式
            JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
            this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                    .client(client)
                    .serializer(serializer)
                    .basePath(ZK_BASE_PATH)
                    .build();
            this.serviceDiscovery.start();
        }catch (Exception e){
            log.error("serviceDiscovery start error :{}", e);
        }

    }
    @Override
    public void register(ServiceInfo serviceInfo) throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .address(serviceInfo.getAddress())// 地址
                .port(serviceInfo.getPort())// 端口
                .name(serviceInfo.getServiceName())// 服务名称
                .payload(serviceInfo)
                .build();

        serviceDiscovery.registerService(serviceInstance);
    }

    @Override
    public void unRegister(ServiceInfo serviceInfo)throws Exception {
        ServiceInstance<ServiceInfo> serviceInstance = ServiceInstance.<ServiceInfo>builder()
                .address(serviceInfo.getAddress())// 地址
                .port(serviceInfo.getPort())// 端口
                .name(serviceInfo.getServiceName())// 服务名称
                .payload(serviceInfo)
                .build();
        serviceDiscovery.unregisterService(serviceInstance);
    }

    @Override
    public void destroy()throws Exception {
        serviceDiscovery.close();
    }
}
