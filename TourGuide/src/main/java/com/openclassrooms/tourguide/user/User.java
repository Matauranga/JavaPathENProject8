package com.openclassrooms.tourguide.user;

import gpsUtil.location.VisitedLocation;
import lombok.Getter;
import tripPricer.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
public class User {
    private final UUID userId;
    private final String userName;
    private String phoneNumber;
    private String emailAddress;
    private Date latestLocationTimestamp;
    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private List<UserReward> userRewards = new ArrayList<>();
    private UserPreferences userPreferences = new UserPreferences();
    private List<Provider> tripDeals = new ArrayList<>();

    public User(UUID userId, String userName, String phoneNumber, String emailAddress) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setLatestLocationTimestamp(Date latestLocationTimestamp) {
        this.latestLocationTimestamp = latestLocationTimestamp;
    }

    public void setUserPreferences(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    public void setTripDeals(List<Provider> tripDeals) {
        this.tripDeals = tripDeals;
    }

    public void addToVisitedLocations(VisitedLocation visitedLocation) {
        visitedLocations.add(visitedLocation);
    }

    public void addUserReward(UserReward userReward) {
        if (userRewards.stream().noneMatch(r -> r.attraction.attractionName.equals(userReward.attraction.attractionName))) {
            userRewards.add(userReward);
        }
    }

    public void clearVisitedLocations() {
        visitedLocations.clear();
    }

    public VisitedLocation getLastVisitedLocation() {
        return visitedLocations.get(visitedLocations.size() - 1);
    }

}
