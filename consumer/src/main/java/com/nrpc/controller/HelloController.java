package com.nrpc.controller;

import com.nrpc.client.annotation.RpcAutowired;
import com.nrpc.interfaces.HelloService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HelloController {
    @RpcAutowired
    HelloService helloService;

    @GetMapping("/hello/{name}")
    public ResponseEntity<String> hello(@PathVariable("name") String name){
        return  ResponseEntity.ok(helloService.sayHello(name + "say:属于小楠的nrpc终于要上线啦"));
    }
}
