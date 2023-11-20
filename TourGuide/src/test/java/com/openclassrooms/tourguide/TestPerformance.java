package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     * The number of users generated for the high volume tests can be easily
     * adjusted via this method:
     *
     * InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     * These tests can be modified to suit new solutions, just as long as the
     * performance metrics at the end of the tests remains consistent.
     *
     * These are performance metrics that we are trying to hit:
     *
     * highVolumeTrackLocation: 100,000 users within 15 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(15) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     * highVolumeGetRewards: 100,000 users within 20 minutes:
     * assertTrue(TimeUnit.MINUTES.toSeconds(20) >=
     * TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */

    @Disabled
    @Test
    public void highVolumeTrackLocation() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        UserService userService = new UserService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), userService);

        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        // Actual score for 100,000 : 200 secondes.
        InternalTestHelper.setInternalUserNumber(5000);

        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        List<User> allUsers = new ArrayList<>();
        allUsers = userService.getAllUsers();


        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        tourGuideService.trackAllUsersLocation(allUsers);

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: "
                + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    @Disabled
    @Test
    public void highVolumeGetRewards() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        UserService userService = new UserService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), userService);

        // Users should be incremented up to 100,000, and test finishes within 20 minutes
        // Actual score for 100,000 : 12 min. 57 sec.
        InternalTestHelper.setInternalUserNumber(2500);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Attraction attraction = gpsUtilService.getAllAttractions().get(0);
        List<User> allUsers = userService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        rewardsService.calculateAllUsersRewards(allUsers);

        for (User user : allUsers) {
            assertFalse(user.getUserRewards().isEmpty());
        }

        stopWatch.stop();
        tourGuideService.tracker.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
                + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
