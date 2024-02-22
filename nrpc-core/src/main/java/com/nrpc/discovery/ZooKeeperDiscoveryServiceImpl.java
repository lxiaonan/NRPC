package com.nrpc.discovery;

import com.nrpc.balancer.LoadBalance;
import com.nrpc.common.ServiceInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.stream.Collectors;

public class ZooKeeperDiscoveryServiceImpl implements DiscoveryService {
    private final int BASE_SLEEP_TIME_MS = 1000;
    private final int MAX_RETRIES = 3;
    private final String ZK_BASE_PATH = "/nrpc";
    private ServiceDiscovery<ServiceInfo> serviceDiscovery;
    private LoadBalance loadBalance;

    public ZooKeeperDiscoveryServiceImpl(String address, LoadBalance balance) {
        this.loadBalance = balance;
        try {
            CuratorFramework client = CuratorFrameworkFactory
                    .newClient(address, new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRIES));
            client.start();
            //将 ServiceInfo 对象序列化为 JSON 格式
            JsonInstanceSerializer<ServiceInfo> serializer = new JsonInstanceSerializer<>(ServiceInfo.class);
            this.serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceInfo.class)
                    .client(client)
                    .serializer(serializer)
                    .basePath(ZK_BASE_PATH)
                    .build();
            this.serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServiceInfo discovery(String serviceName) throws Exception {
        // 实现ZooKeeper服务发现逻辑
        Collection<ServiceInstance<ServiceInfo>> instances = serviceDiscovery.queryForInstances(serviceName);

        return instances.isEmpty()
                ? null : loadBalance.
                selectOne(instances.stream().map(ServiceInstance::getPayload).collect(Collectors.toList()));
    }
}
