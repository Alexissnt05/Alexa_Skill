package Handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RepeatStopsIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("repeatStopsIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        AttributesManager attributesManager = input.getAttributesManager();
        Map<String, Object> sessionAttributes = attributesManager.getSessionAttributes();
        HashMap<Integer, HashMap<String, String>> retrievedStops = (HashMap<Integer, HashMap<String, String>>) sessionAttributes.get("stops");
        StringBuilder speechText = new StringBuilder();
        int index = 1;
        for (Integer ignored : retrievedStops.keySet()) {
            speechText.append("Stop number " + index + " is located at " + retrievedStops.get(index).values().iterator().next() + ". ");
            index++;
        }
        speechText.append("You can say, check stop number 1, or set stop number 1 as default.");
        return input.getResponseBuilder().withSpeech(speechText.toString()).withShouldEndSession(false).build();
    }
}
