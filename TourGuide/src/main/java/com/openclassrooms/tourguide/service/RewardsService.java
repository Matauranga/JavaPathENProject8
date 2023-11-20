package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    // proximity in miles
    private final int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilService gpsUtilService;
    private final RewardCentral rewardsCentral;
    private final ExecutorService executor = Executors.newFixedThreadPool(2048);
    private UserService userService;

    public RewardsService(GpsUtilService gpsUtilService, RewardCentral rewardCentral, UserService userService) {
        this.userService = userService;
        this.gpsUtilService = gpsUtilService;
        this.rewardsCentral = rewardCentral;
    }

    /**
     * Method to set proximity buffer
     *
     */
    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    /**
     * Method to set default proximity buffer
     *
     */
    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     *
     */
    public void calculateAllUsersRewards(List<User> users) {
        List<CompletableFuture<Void>> futures = users
                .parallelStream()
                .map(user -> CompletableFuture.runAsync(() -> calculateRewards(user), executor))
                .toList();

        futures.forEach(CompletableFuture::join);
    }


    /**
     * This method retrieves all attractions not rewarded but visited by a user and calculates the new rewards
     *
     */
    public void calculateRewards(User user) {
        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtilService.getAllAttractions();

        List<CompletableFuture> futures = new ArrayList<>();

        userLocations
                .parallelStream()
                .forEach(visitedLocation -> getNotRewardAttractions(user, attractions)
                        .parallelStream()
                        .filter(attraction -> nearAttraction(visitedLocation, attraction))
                        .forEach(attraction -> submitReward(user, visitedLocation, attraction)));
//                        .forEach(attraction -> futures.add(CompletableFuture.runAsync(() -> submitReward(user, visitedLocation, attraction), executor))));

        futures.forEach(future -> {
            try {
                future.get();
            } catch (Exception e) {
                log.error("Calculate Rewards error : " + e.getMessage());
            }
        });
    }


    /**
     * Method to retrieve all unrewarded attractions for a user
     *
     */
    private static List<Attraction> getNotRewardAttractions(User user, List<Attraction> attractions) {
        return attractions
                .parallelStream()
                .filter(attraction -> user.getUserRewards()
                        .parallelStream()
                        .noneMatch(userReward -> userReward.attraction.attractionName.equals(attraction.attractionName))
                )
                .toList();
    }

    /**
     * This method submits rewards for visiting an attraction
     *
     */
    private void submitReward(User user, VisitedLocation visitedLocation, Attraction attraction) {
        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
    }

    /**
     * This method allows us to know is location is in the attraction proximity range
     *
     */
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }

    /**
     * This method allows us to know if a user is close to an attraction
     *
     */
    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
    }

    /**
     * This method gives us the number of reward points for visiting an attraction
     *
     */
    public int getRewardPoints(Attraction attraction, UUID userId) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userId);
    }

    /**
     * This method returns the distance (in miles) between a user and an attraction
     *
     */
    public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
    }

}
