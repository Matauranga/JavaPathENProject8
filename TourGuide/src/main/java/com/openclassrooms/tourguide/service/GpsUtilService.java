package com.openclassrooms.tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GpsUtilService {

    public GpsUtilService() {
    }

    public List<Attraction> getAllAttractions() {
        GpsUtil gpsUtil = new GpsUtil();
        return gpsUtil.getAttractions();
    }

    public VisitedLocation getUserLoc(UUID userId) {
        GpsUtil gpsUtil = new GpsUtil();
        return gpsUtil.getUserLocation(userId);
    }

}
