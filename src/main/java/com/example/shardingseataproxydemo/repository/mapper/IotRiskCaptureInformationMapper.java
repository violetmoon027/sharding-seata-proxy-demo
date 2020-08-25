package com.example.shardingseataproxydemo.repository.mapper;

import com.example.shardingseataproxydemo.repository.model.IotRiskCaptureInformation;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @ClassName IotRiskCaptureInformationMapper
 * @Author sipeng
 * @Date 2020/8/25 3:36 下午
 * @Version 1.0
 */
@Repository
public interface IotRiskCaptureInformationMapper {

    @Select("select info_id, tenant_id, channel, person_id, trip_id, time, push_time, group_id, face_id, capture_id, face_img, device_id, capture_time, is_first, hat, glass, age, gender, helmet, race, pitch, yaw, blur, border, align_score, image, image_width, image_height, x, y, height, width from iot_risk_capture_information WHERE( capture_id = #{captureId}) limit 1")
    IotRiskCaptureInformation selectByCaptureId(@Param("captureId") String captureId);
}
