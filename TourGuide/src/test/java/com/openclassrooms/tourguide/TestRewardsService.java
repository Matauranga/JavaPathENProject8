package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import rewardCentral.RewardCentral;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class TestRewardsService {

    @Test
    public void userGetRewards() {
        GpsUtilService gpsUtilService = new GpsUtilService();

        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        Attraction attraction = gpsUtilService.getAllAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        tourGuideService.tracker.stopTracking();
        assertTrue(userRewards.size() == 1);
    }

    @Test
    public void isWithinAttractionProximity() {
        GpsUtilService gpsUtilService = new GpsUtilService();

        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        Attraction attraction = gpsUtilService.getAllAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void nearAllAttractions() throws ExecutionException, InterruptedException {
        GpsUtilService gpsUtilService = new GpsUtilService();

        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral());
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(1);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        rewardsService.calculateRewards(userService.getAllUsers().get(0));
        List<UserReward> userRewards = tourGuideService.getUserRewards(userService.getAllUsers().get(0));
        tourGuideService.tracker.stopTracking();

        assertEquals(gpsUtilService.getAllAttractions().size(), userRewards.size());
    }

}
