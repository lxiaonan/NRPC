package com.nrpc.controller;

import com.nrpc.client.annotation.RpcAutowired;
import com.nrpc.interfaces.LuckyIndexService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LuckyIndexController {
    @RpcAutowired
    LuckyIndexService luckyIndexService;
    @GetMapping("/getLucky")
    public ResponseEntity<String> getLuckyIndex(){
        return ResponseEntity.ok("您今天的幸运值是：" + luckyIndexService.getLuckyIndex());
    }
}
