package com.openclassrooms.tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Class to load GpsUtil once
 *
 */
@Service
public class GpsUtilService {
    private GpsUtil gpsUtil = new GpsUtil();

    /**
     * Method to get all attractions
     *
     */
    public List<Attraction> getAllAttractions() {
        return gpsUtil.getAttractions();
    }

    /**
     * Method to get user location
     *
     */
    public VisitedLocation getUserLoc(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }

}
