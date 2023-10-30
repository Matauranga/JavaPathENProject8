package com.openclassrooms.tourguide.DTO;

import gpsUtil.location.Location;
import lombok.Getter;

@Getter
public class AttractionDTO {

    private final String attractionName;
    private final Location attractionGPSCoordinates;
    private final Location userGPSCoordinates;
    private final Double distance;
    private final Integer rewardPoints;

    public AttractionDTO(String attractionName, Location attractionGPSCoordinates, Location userGPSCoordinates, Double distance, Integer rewardPoints) {
        this.attractionName = attractionName;
        this.attractionGPSCoordinates = attractionGPSCoordinates;
        this.userGPSCoordinates = userGPSCoordinates;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }
}
