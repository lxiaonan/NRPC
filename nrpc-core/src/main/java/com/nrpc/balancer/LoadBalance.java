package com.nrpc.balancer;

import com.nrpc.common.ServiceInfo;

import java.util.List;

public interface LoadBalance {
    ServiceInfo selectOne(List<ServiceInfo> serviceInfos);
}
