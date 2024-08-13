package Handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import org.alexis.TransitArrivalInfo;

import java.util.Optional;

public class CheckDefaultStopIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("checkDefaultStop"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        AttributesManager attributesManager = input.getAttributesManager();

        try {
            String globalStopId = attributesManager.getPersistentAttributes().get("defaultStop").toString();
            TransitArrivalInfo.setNextTransport(globalStopId);
        } catch (NullPointerException exception) {
            return input.getResponseBuilder().withSpeech("You haven't set a default stop yet. You need to search" +
                            " for stops, and then set one as default. You can say, search for stops.").withShouldEndSession(false)
                    .build();
        }
        return input.getResponseBuilder().withSpeech(getSpeechText()).build();
    }

    public String getSpeechText() {
        return TransitArrivalInfo.getTimeUnit().equals("seconds") ? TransitArrivalInfo.getTransitType() + " " +
                TransitArrivalInfo.getTransitNumber() + " " + TransitArrivalInfo.getTransitName() + " is arriving in "
                + TransitArrivalInfo.getTimeUnit() : TransitArrivalInfo.getTransitType() + " " +
                TransitArrivalInfo.getTransitNumber() + " " + TransitArrivalInfo.getTransitName() +
                " is arriving in " + TransitArrivalInfo.getDepartureTime() + " " + TransitArrivalInfo.getTimeUnit();
    }



}
