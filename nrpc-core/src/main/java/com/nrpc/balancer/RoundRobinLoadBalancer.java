package com.nrpc.balancer;

import com.nrpc.common.ServiceInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalance {
    AtomicInteger integer = new AtomicInteger(0);
    @Override
    public ServiceInfo selectOne(List<ServiceInfo> serviceInfos) {
        int idx = integer.getAndIncrement() % serviceInfos.size();
        return serviceInfos.get(idx);
    }
}
