package com.openclassrooms.tourguide;

import com.openclassrooms.tourguide.DTO.AttractionDTO;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.RewardsService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import gpsUtil.location.VisitedLocation;
import org.junit.jupiter.api.Test;
import rewardCentral.RewardCentral;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTourGuideService {

    @Test
    public void getUserLocation() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();
        assertTrue(visitedLocation.userId.equals(user.getUserId()));
    }

    @Test
    public void addUser() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);

        User retrivedUser = userService.getUser(user.getUserName());
        User retrivedUser2 = userService.getUser(user2.getUserName());

        tourGuideService.tracker.stopTracking();

        assertEquals(user, retrivedUser);
        assertEquals(user2, retrivedUser2);
    }

    @Test
    public void getAllUsers() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);

        List<User> allUsers = userService.getAllUsers();

        tourGuideService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void trackUser() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getNearbyAttractions() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = tourGuideService.trackUserLocation(user);

        List<AttractionDTO> attractions = tourGuideService.getNearbyAttractions(visitedLocation);

        for (AttractionDTO attractionDTO : attractions) {
            System.out.println("TEST ||||| Name : " + attractionDTO.getAttractionName() + " | Distance : " + attractionDTO.getDistance());
        }

        tourGuideService.tracker.stopTracking();

        assertEquals(5, attractions.size());
    }

    @Test
    public void getTripDeals() {
        GpsUtilService gpsUtilService = new GpsUtilService();
        RewardsService rewardsService = new RewardsService(gpsUtilService, new RewardCentral(), new UserService());
        UserService userService = new UserService();
        InternalTestHelper.setInternalUserNumber(0);
        TourGuideService tourGuideService = new TourGuideService(gpsUtilService, rewardsService, userService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = tourGuideService.getTripDeals(user);

        tourGuideService.tracker.stopTracking();

        assertEquals(5, providers.size());
    }

}
