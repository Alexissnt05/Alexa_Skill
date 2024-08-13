package Handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.request.RequestHelper;
import org.alexis.TransitArrivalInfo;

import java.util.Optional;

public class CheckStopIntentHandler implements IntentRequestHandler { //IntentRequestHandler handles an incoming Intent request.

    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        return handlerInput.matches(Predicates.intentName("CheckStopIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        RequestHelper requestHelper = RequestHelper.forHandlerInput(handlerInput);
        Optional<String> slotValue = requestHelper.getSlotValue("stopNumber");

        if (slotValue.isEmpty()) {
            return handlerInput.getResponseBuilder().withSpeech("Sorry, you didn't provide a stop number. Please try again.").withShouldEndSession(false).build();
        }

        int stopNumber = Integer.parseInt(slotValue.get());
        if (SearchStopsIntentHandler.stops.isEmpty()) {
            return handlerInput.getResponseBuilder().withSpeech("Sorry, you need to search for stops first." +
                    " You can say, search for stops.").withShouldEndSession(false).build();
        }
        String globalStopId = SearchStopsIntentHandler.stops.get(stopNumber).keySet().iterator().next();
        TransitArrivalInfo.setNextTransport(globalStopId);
        return handlerInput.getResponseBuilder().withSpeech(getSpeechText()).build();
    }

    public static String getSpeechText() {
        return TransitArrivalInfo.getTimeUnit().equals("seconds") ? TransitArrivalInfo.getTransitType() + " " +
                TransitArrivalInfo.getTransitNumber() + " " + TransitArrivalInfo.getTransitName() + " is arriving in "
                + TransitArrivalInfo.getTimeUnit() : TransitArrivalInfo.getTransitType() + " " +
                TransitArrivalInfo.getTransitNumber() + " " + TransitArrivalInfo.getTransitName() +
                " is arriving in " + TransitArrivalInfo.getDepartureTime() + " " + TransitArrivalInfo.getTimeUnit() + ".";
    }
}
