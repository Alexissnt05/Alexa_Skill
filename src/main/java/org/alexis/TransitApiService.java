package org.alexis;


import Handlers.SearchStopsIntentHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;

public class TransitApiService {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String endPoint = "https://external.transitapp.com/v3/public/stop_departures?global_stop_id=";
    private static final String apiKey = System.getenv("API_KEY");

    public static String getUpcomingDeparturesResponse(String globalStopId) {
        Request request = new Request.Builder()
                .url(endPoint + globalStopId)
                .addHeader("apiKey", apiKey)
                .build();
        try {
            return client.newCall(request).execute().body().string();
        } catch (IOException e) {
            return "IOException!";
        }
    }

//    returns a Json String with a list of stops around a location
    public static String getNearbyStopsResponse() {
        Request request = new Request.Builder().url("https://external.transitapp.com/v3/public/nearby_stops?" + SearchStopsIntentHandler.getLatLong())
                .addHeader("apiKey", apiKey)
                .build();
        try {
            return client.newCall(request).execute().body().string();
        } catch (IOException e) {
            return "IOException!";
        }
    }

    public static String getGlobalStopIdResponse(String stopCode) {
        Request request = new Request.Builder().url("https://external.transitapp.com/v3/public/search_stops?" +
                        "lat=39.833851&lon=-74.871826&query=" + stopCode)
                .addHeader("apiKey", apiKey)
                .build();
        try {
            return client.newCall(request).execute().body().string();
        } catch (IOException e) {
            return "IOException!";
        }
    }
}
