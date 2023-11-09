package com.openclassrooms.tourguide.config;

import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import rewardCentral.RewardCentral;

@Configuration
public class TourGuideModule {

    @Bean
    public GpsUtilService getGpsUtilService() {
        return new GpsUtilService();
    }

    @Bean
    public RewardsService getRewardsService() {
        return new RewardsService(getGpsUtilService(), getRewardCentral());
    }

    @Bean
    public RewardCentral getRewardCentral() {
        return new RewardCentral();
    }

}
