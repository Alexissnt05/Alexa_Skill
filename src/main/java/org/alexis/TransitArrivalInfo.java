package org.alexis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TransitArrivalInfo {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static JsonNode node;
    private static String transitName;
    private static String transitType;
    private static long departureTime;
    private static String transitNumber;
    private static String timeUnit;


    private static void setJsonNode(String globalStopId) {
        try {
            node = objectMapper.readTree(TransitApiService.getUpcomingDeparturesResponse(globalStopId));
        } catch (JsonProcessingException ignored) {
        }
    }


    //initializes departureTime to the difference between the current time and departureTime in unix
    public static void setNextTransport(String globalStopId) {
        setJsonNode(globalStopId);
        long departureTimeInUnix = getDepartureTimeInUnix();
        // Set departureTime to the difference in minutes between now and the next departure time
        departureTime = ChronoUnit.MINUTES.between(Instant.now(), Instant.ofEpochSecond(departureTimeInUnix));
        //if departureTime is greater than 60 minutes
        if (departureTime >= 60) {
            departureTime = departureTime / 60;
            if (departureTime == 1) {
                timeUnit = "hour";
            } else {
                timeUnit = "hours";
            } //if departure time is equal to 0 (means the transportation is seconds away)
        } else if (departureTime == 0) {
            timeUnit = "seconds";
        } else if (departureTime == 1) {
            timeUnit = "minute";
        } else {
            timeUnit = "minutes";
        }
    }

    //initializes class variables with data about the earliest departure time and returns the departureTime in unix
    private static long getDepartureTimeInUnix() {
        long departureTimeInUnix = Long.MAX_VALUE;
        JsonNode routeDeparturesArray = node.get("route_departures");
        //triple-nested for loop to retrieve the earliest departure time of a public transportation
        for (int i = 0; i < routeDeparturesArray.size(); i++) {
            JsonNode itinerariesArray = routeDeparturesArray.get(i).get("itineraries");
            for (int j = 0; j < itinerariesArray.size(); j++) {
                JsonNode scheduleItemsArray = itinerariesArray.get(j).get("schedule_items");
                for (int k = 0; k < scheduleItemsArray.size(); k++) {
                    long departureTime = scheduleItemsArray.get(k).get("departure_time").asLong();
                    if (departureTime < departureTimeInUnix) {
                        departureTimeInUnix = departureTime;
                        transitName = itinerariesArray.get(j).get("headsign").asText();
                        transitType = routeDeparturesArray.get(i).get("mode_name").asText();
                        transitNumber = routeDeparturesArray.get(i).get("route_short_name").asText();
                    }
                }
            }
        }
        return departureTimeInUnix;
    }

    public static String getTransitName() {
        return transitName;
    }

    public static long getDepartureTime() {
        return departureTime;
    }

    public static String getTransitType() {
        return transitType;
    }

    public static String getTransitNumber() {
        return transitNumber;
    }

    public static String getTimeUnit() {
        return timeUnit;
    }
}
