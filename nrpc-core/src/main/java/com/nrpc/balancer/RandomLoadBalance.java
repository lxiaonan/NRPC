package com.nrpc.balancer;

import com.nrpc.common.ServiceInfo;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {
    @Override
    public ServiceInfo selectOne(List<ServiceInfo> serviceInfos) {
        Random random = new Random();
        return serviceInfos.get(random.nextInt(serviceInfos.size()));
    }
}
