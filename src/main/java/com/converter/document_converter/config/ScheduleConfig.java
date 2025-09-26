package com.converter.document_converter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ScheduleConfig {
    // 스케줄링 활성화
    // UserService의 @Scheduled 메서드가 자동으로 실행됨
    // 매일 자정 00:00에 일일 사용량 초기화
}