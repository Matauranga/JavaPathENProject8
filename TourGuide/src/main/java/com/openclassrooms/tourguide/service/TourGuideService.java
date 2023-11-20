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

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

/**
 * Class to link user, rewards and location.
 *
 */
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
    private final ExecutorService executor = Executors.newFixedThreadPool(2048);

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

    /**
     * Method to get user rewards
     *
     * @return a list of user rewards
     */
    public List<UserReward> getUserRewards(User user) {//TODO inutile
        return user.getUserRewards();
    }

    /**
     *Method to get user location
     *
     * @return the user location
     */
    public VisitedLocation getUserLocation(User user) {
        return (!user.getVisitedLocations().isEmpty())
                ? user.getLastVisitedLocation()
                : trackUserLocation(user);
    }

    /**
     *Method to get trip deal
     *
     * @return a list of trip deal
     */
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

    /**
     * Method to get all user location/ moi
     *
     */
    public List<VisitedLocation> trackAllUsersLocation(List<User> userList) {

        List<CompletableFuture<VisitedLocation>> usersLocations =
                userList
                        .parallelStream()
                        .map(user -> CompletableFuture.supplyAsync(() -> trackUserLocation(user), executor))
                        .toList();

        return usersLocations
                .parallelStream()
                .map(CompletableFuture::join)
                .collect(toList());
    }

    /**
     * Method to get user location
     *
     * @return the localisation of the user
     */
    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtilService.getUserLoc(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    /**
     * Get the closest five tourist attractions to the user - no matter how far away they are
     *
     * @param visitedLocation user's last known location
     * @return A list of objects with the name of the attractions, the attractions and the user's location, the distance between the user and the attractions and the reward points earned for visiting these attractions
     */
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

    /**
     * Assures to shut down the Tracker thread
     *
     */
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

}
