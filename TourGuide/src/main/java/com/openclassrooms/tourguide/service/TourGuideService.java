package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.DTO.AttractionDTO;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TourGuideService {
    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
    private final GpsUtilService gpsUtilService;

    //private final GpsUtil gpsUtil;
    private static final String tripPricerApiKey = "test-server-api-key";
    private final UserService userService;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;

    public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService, UserService userService) {
        //this.gpsUtil = gpsUtil;
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;
        this.userService = userService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            userService.initializeInternalUsers();
            logger.debug("Finished initializing users");
        }
        tracker = new Tracker(this, userService);
        addShutDownHook();
    }


    public List<UserReward> getUserRewards(User user) {//TODO inutile
        return user.getUserRewards();
    }


    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty()) ? user.getLastVisitedLocation()
                : trackUserLocation(user);
    }


    public List<Provider> getTripDeals(User user) {

        int cumulativeRewardPoints = user.getUserRewards()
                .stream()
                .mapToInt(UserReward::getRewardPoints)
                .sum();

        List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
                user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
                user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);

        user.setTripDeals(providers);

        return providers;
    }


    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLoc(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }


    /**
     * Original
     */
//    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
//        List<Attraction> nearbyAttractions = new ArrayList<>();
//        for (Attraction attraction : gpsUtil.getAttractions()) {
//            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
//                nearbyAttractions.add(attraction);
//            }
//        }
//
//        return nearbyAttractions;
//    }


    /**
     * mine
     */

    //TODO : Rename ? getFiveClosestAttractions
    public List<AttractionDTO> getNearbyAttractions(VisitedLocation visitedLocation) {

        return gpsUtilService.getAllAttractions()
                .parallelStream()
                .sorted(Comparator.comparing(attraction -> rewardsService.getDistance(attraction, visitedLocation.location)))
                .limit(5)
                .map(attraction -> new AttractionDTO(attraction.attractionName,
                        attraction,
                        visitedLocation.location,
                        rewardsService.getDistance(attraction, visitedLocation.location),
                        rewardsService.getRewardPoints(attraction, visitedLocation.userId)))
                .toList();

    }


    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }


}
