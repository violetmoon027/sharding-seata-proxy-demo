package com.example.shardingseataproxydemo.controller;

import com.example.shardingseataproxydemo.repository.model.IotRiskCaptureInformation;
import com.example.shardingseataproxydemo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Author sipeng
 * @Date 2020/8/25 3:41 下午
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/{captureId}")
    public IotRiskCaptureInformation test(@PathVariable("captureId") String captureId) {
        return testService.test(captureId);
    }
}
