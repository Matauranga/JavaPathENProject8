package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    // proximity in miles
    private final int defaultProximityBuffer = 10;
    private int proximityBuffer = defaultProximityBuffer;
    private int attractionProximityRange = 200;
    private final GpsUtilService gpsUtilService;
    private final RewardCentral rewardsCentral;
    private final ExecutorService executor = Executors.newFixedThreadPool(1000);

    public RewardsService(GpsUtilService gpsUtilService, RewardCentral rewardCentral) {
        this.gpsUtilService = gpsUtilService;
        this.rewardsCentral = rewardCentral;
    }

    public void setProximityBuffer(int proximityBuffer) {
        this.proximityBuffer = proximityBuffer;
    }

    public void setDefaultProximityBuffer() {
        proximityBuffer = defaultProximityBuffer;
    }

    /**
     * Original
     */
//	public void calculateRewards(User user) {
//		List<VisitedLocation> userLocations = user.getVisitedLocations();
//		List<Attraction> attractions = gpsUtil.getAttractions();
//
//		for(VisitedLocation visitedLocation : userLocations) {
//			for(Attraction attraction : attractions) {
//				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
//					if(nearAttraction(visitedLocation, attraction)) {
//						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
//					}
//				}
//			}
//		}
//	}

    /**
     * Mine
     */
    public void calculateRewards(User user) {

        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
        List<Attraction> attractions = gpsUtilService.getAllAttractions();

        var notRewardAttractions = attractions
                .stream()
                .filter(attraction -> user.getUserRewards()
                        .stream()
                        .noneMatch(userReward -> userReward.attraction.attractionName.equals(attraction.attractionName))
                )
                .toList();

        userLocations
                .parallelStream()
                .forEach(visitedLocation -> notRewardAttractions
                        .parallelStream()
                        .filter(attraction -> nearAttraction(visitedLocation, attraction))
                        //.forEach(attraction -> submitReward(user, visitedLocation, attraction)));
                        .forEach(attraction -> user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())))));

    }


    private void submitReward(User user, VisitedLocation visitedLocation, Attraction attraction) {//TODO a corriger

        CompletableFuture.supplyAsync(() -> getRewardPoints(attraction, user.getUserId()), executor)//TODO Frank
                .thenAccept(p -> {
                    var rewards = new UserReward(visitedLocation, attraction, p);
                    user.addUserReward(rewards);
                });

    }

    /**
     * Mine old version
     */
//    public void calculateRewards(User user) {//TODO : Franck --> test nearAllAttractions
//
//        List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
//        List<Attraction> attractions = gpsUtil.getAttractions();
//
//        for (VisitedLocation visitedLocation : userLocations) {
//            for (Attraction attraction : attractions) {
//                if (user.getUserRewards().stream().noneMatch(userReward -> userReward.attraction.attractionName.equals(attraction.attractionName))) {
//                    if (nearAttraction(visitedLocation, attraction)) {
//                        user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user.getUserId())));
//                    }
//                }
//            }
//        }
//
//    }
    public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
        return !(getDistance(attraction, location) > attractionProximityRange);
    }

    private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
        return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
    }

    public int getRewardPoints(Attraction attraction, UUID userId) {
        return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, userId);
    }

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
