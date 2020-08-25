package com.example.shardingseataproxydemo.service;

import com.example.shardingseataproxydemo.repository.mapper.IotRiskCaptureInformationMapper;
import com.example.shardingseataproxydemo.repository.model.IotRiskCaptureInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName TestService
 * @Author sipeng
 * @Date 2020/8/25 3:40 下午
 * @Version 1.0
 */
@Service
public class TestService {

    @Autowired
    IotRiskCaptureInformationMapper iotRiskCaptureInformationMapper;


    public IotRiskCaptureInformation test(String captureId) {
        return iotRiskCaptureInformationMapper.selectByCaptureId(captureId);
    }

}
