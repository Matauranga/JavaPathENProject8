package com.openclassrooms.tourguide.controller;

import com.openclassrooms.tourguide.DTO.AttractionDTO;
import com.openclassrooms.tourguide.service.GpsUtilService;
import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.service.UserService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tripPricer.Provider;

import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class TourGuideController {

    @Autowired
    TourGuideService tourGuideService;
    @Autowired
    UserService userService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public VisitedLocation getLocation(@RequestParam String userName) {
        return tourGuideService.getUserLocation(getUser(userName));
    }

    @RequestMapping("/getNearbyAttractions")
    public List<AttractionDTO> getNearbyAttractions(@RequestParam String userName) {
        VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
        return tourGuideService.getNearbyAttractions(visitedLocation);
    }

    @RequestMapping("/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        return tourGuideService.getUserRewards(getUser(userName));
    }

    @RequestMapping("/getTripDeals")
    public List<Provider> getTripDeals(@RequestParam String userName) {
        return tourGuideService.getTripDeals(getUser(userName));
    }

    private User getUser(String userName) {
        return userService.getUser(userName);
    }

    /*************************************************************
     *
     *                       EndPoint test
     *
     ************************************************************/
    @Autowired
    GpsUtilService gpsUtilService;

    /**
     *
     * This creates a user and gives them an attraction reward
     *
     * @param user
     */
    @PostMapping("/addUser")
    public void createUser(@RequestBody User user) {
        log.debug("Ask to create person");
        userService.addUser(user);

        Attraction attraction = gpsUtilService.getAllAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        tourGuideService.trackUserLocation(user);
        tourGuideService.tracker.stopTracking();

    }

}