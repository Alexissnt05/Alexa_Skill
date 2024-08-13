package Handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.request.RequestHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SetDefaultStopIntentHandler implements IntentRequestHandler {
    @Override
    public boolean canHandle(HandlerInput handlerInput, IntentRequest intentRequest) {
        return handlerInput.matches(Predicates.intentName("setDefaultStopIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, IntentRequest intentRequest) {
        RequestHelper requestHelper = RequestHelper.forHandlerInput(handlerInput);
        Optional<String> stopNumber = requestHelper.getSlotValue("stopNumber"); //retrieves the value of slot "StopNumber"

        if (stopNumber.isEmpty()) {
            return handlerInput.getResponseBuilder().withSpeech("Sorry, it looks like you didn't provide a stop number. " +
                            "You can say something like, set stop number 1 as default.")
                    .withShouldEndSession(false)
                    .build();
        }

        AttributesManager attributesManager = handlerInput.getAttributesManager();
        Map<String, Object> sessionAttributes = attributesManager.getSessionAttributes(); //retrieves the session attributes
        HashMap<Integer, HashMap<String, String>> retrievedStops = (HashMap<Integer, HashMap<String, String>>) sessionAttributes.get("stops"); //initializes the retrievedStops HashMap with values from "stops" session attribute
        boolean isPresent = retrievedStops.containsKey(Integer.valueOf(stopNumber.get()));
        if (retrievedStops.isEmpty()) { //if its empty then user invoked this intent without searching for stops first
            return handlerInput.getResponseBuilder().withSpeech("Sorry, you need to search for stops first. " +
                            "try saying, search for stops near me.")
                            .withShouldEndSession(false).build();
        } else if (!isPresent) { //checks if the key is not present in the retrieveStops HashMap or if the user provides a stop number out the range.
            return handlerInput.getResponseBuilder().withSpeech("Sorry, that's not a valid option. Please " +
                            "choose a stop number within the available range.")
                    .withShouldEndSession(false).build();
        }

        String defaultStop = null; //holds the global stop id
        try {
            defaultStop = retrievedStops.get(Integer.valueOf(stopNumber.get())).keySet().iterator().next();
        } catch (NoSuchElementException ignored) {
        }

        if (defaultStop != null) {
            AttributesManager persistentAttManager = handlerInput.getAttributesManager();
            Map<String, Object> persistentAttributes = new HashMap<>();
            persistentAttributes.put("defaultStop", defaultStop);
            persistentAttManager.setPersistentAttributes(persistentAttributes); //adds the defaultStop as a persistent attribute to the DynamoDB table
            persistentAttManager.savePersistentAttributes();
            String speechText = "You have set your default stop to stop number " + stopNumber.get() + " located" +
                    " at " + retrievedStops.get(Integer.valueOf(stopNumber.get())).values().iterator().next() + ".";
            return handlerInput.getResponseBuilder().withSpeech(speechText).build();
        }
        return handlerInput.getResponseBuilder().withSpeech("Sorry, I'm having issues setting your default stop.").build();

    }


}
