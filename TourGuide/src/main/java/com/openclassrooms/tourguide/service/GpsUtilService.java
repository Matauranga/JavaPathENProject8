package com.openclassrooms.tourguide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GpsUtilService {
    private GpsUtil gpsUtil = new GpsUtil();

    public List<Attraction> getAllAttractions() {
        return gpsUtil.getAttractions();
    }

    public VisitedLocation getUserLoc(UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }

}
