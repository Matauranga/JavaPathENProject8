package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.DTO.AttractionDTO;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class TourGuideService {
    private final GpsUtilService gpsUtilService;
    private static final String tripPricerApiKey = "test-server-api-key";
    private final UserService userService;
    private final RewardsService rewardsService;
    private final TripPricer tripPricer = new TripPricer();
    public final Tracker tracker;
    boolean testMode = true;
    private final ExecutorService executor = Executors.newFixedThreadPool(1024);

    public TourGuideService(GpsUtilService gpsUtilService, RewardsService rewardsService, UserService userService) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsService = rewardsService;
        this.userService = userService;

        Locale.setDefault(Locale.US);

        if (testMode) {
            log.info("TestMode enabled");
            log.debug("Initializing users");
            userService.initializeInternalUsers();
            log.debug("Finished initializing users");
        }
        tracker = new Tracker(this, userService);
        addShutDownHook();
    }

    public List<UserReward> getUserRewards(User user) {//TODO inutile
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty())
                ? user.getLastVisitedLocation()
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

    public void trackAllUsersLocation() {
        var users = userService.getAllUsers();

        List<CompletableFuture> futures = new ArrayList<>();

        users.parallelStream()
                .forEach(u -> futures.add(CompletableFuture.supplyAsync(() -> trackUserLocation(u), executor)));

        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                log.error("User tracking error : " + e.getMessage());
            }
        });
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLoc(user.getUserId());
        CompletableFuture.runAsync(() -> {
            user.addToVisitedLocations(visitedLocation);
            rewardsService.calculateRewards(user);
        }, executor);
        return visitedLocation;
    }

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
