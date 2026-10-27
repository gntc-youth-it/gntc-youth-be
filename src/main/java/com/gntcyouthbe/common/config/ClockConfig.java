package com.gntcyouthbe.common.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // 지금 시각이 필요한 곳은 이 Clock을 주입받아서 테스트에서 시각을 고정할 수 있게 한다.
    // 운영 서버처럼 UTC로 두고, 한국 날짜와 시각이 필요하면 쓰는 쪽에서 Asia/Seoul로 바꾼다.
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
