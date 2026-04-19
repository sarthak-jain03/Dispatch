package com.matching_service.service;


import com.matching_service.dto.NearbyDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class DriverScoringService {


    private static final double WEIGHT_DISTANCE     = 40.0;
    private static final double WEIGHT_RATING       = 35.0;
    private static final double WEIGHT_EXPERIENCE   = 15.0;
    private static final double WEIGHT_RELIABILITY  = 10.0;


    private static final double MAX_RADIUS_KM = 5.0;


    public List<NearbyDriver> scoreAndRank(List<NearbyDriver> candidates) {

        enrichWithDriverProfiles(candidates);

        for (NearbyDriver driver : candidates) {
            double score = calculateScore(driver);
            driver.setScore(score);

            log.debug("Driver {} → dist={}km, rating={}, trips={}, cancel={}% → score={}",
                    driver.getDriverId(),
                    driver.getDistanceKm(),
                    driver.getRating(),
                    driver.getTripsCompleted(),
                    String.format("%.0f", driver.getCancellationRate() * 100),
                    String.format("%.2f", score));
        }

        candidates.sort(Comparator.comparingDouble(NearbyDriver::getScore).reversed());

        return candidates;
    }

    private double calculateScore(NearbyDriver driver) {


        double distanceScore = 1.0 - (driver.getDistanceKm() / MAX_RADIUS_KM);
        distanceScore = Math.max(0, distanceScore);


        double ratingScore = driver.getRating() / 5.0;

        double experienceScore = Math.min(driver.getTripsCompleted() / 1000.0, 1.0);

        double reliabilityScore = 1.0 - driver.getCancellationRate();
        reliabilityScore = Math.max(0, reliabilityScore);

        double score = (distanceScore   * WEIGHT_DISTANCE)
                + (ratingScore     * WEIGHT_RATING)
                + (experienceScore * WEIGHT_EXPERIENCE)
                + (reliabilityScore* WEIGHT_RELIABILITY);

        return Math.round(score * 100.0) / 100.0;
    }


    private void enrichWithDriverProfiles(List<NearbyDriver> drivers) {
        Random random = new Random();

        String[] names = {
                "Ramesh Kumar", "Suresh Patel", "Ajay Sharma", "Vijay Singh",
                "Ravi Verma", "Mohan Das", "Arjun Rao", "Deepak Joshi"
        };

        for (int i=0; i<drivers.size(); i++) {
            NearbyDriver driver = drivers.get(i);

            driver.setName(names[i % names.length]);


            double rating = 3.5 + (random.nextDouble() * 1.5);
            driver.setRating(Math.round(rating * 10.0) / 10.0);

            driver.setTripsCompleted(50 + random.nextInt(1451));

            driver.setCancellationRate(random.nextDouble() * 0.15);
        }
    }
}
