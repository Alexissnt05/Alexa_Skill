package Handlers;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.request.RequestHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alexis.TransitApiService;
import org.alexis.TransitArrivalInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SearchStopByCodeIntentHandler implements IntentRequestHandler {
    private static String defaultStop;

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("SearchStopByCodeIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        return input.getResponseBuilder().withSpeech("To check a specific stop, please provide the stop code. " +
                "For example, you can say, 'My stop code is 1 0 0 8 7").withShouldEndSession(false).build();
    }

    static class SearchByCode implements IntentRequestHandler {

        @Override
        public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
            return input.matches(Predicates.intentName("checkStopCodeIntent"));
        }

        @Override
        public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
            RequestHelper requestHelper = RequestHelper.forHandlerInput(input);
            Optional<String> stopCode = requestHelper.getSlotValue("stopCode");
            if (stopCode.isPresent()) {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jsonNode = objectMapper.readTree(TransitApiService.getGlobalStopIdResponse(stopCode.get()));
                    String globalStopId = jsonNode.get("results").get(0).get("global_stop_id").asText();
                    defaultStop = globalStopId;
                    TransitArrivalInfo.setNextTransport(globalStopId);
                    return input.getResponseBuilder().withSpeech(CheckStopIntentHandler.getSpeechText() + ". " +
                            "If you want to set this stop as default, say, set as default.").withShouldEndSession(false).build();
                } catch (JsonProcessingException | NullPointerException exception) {
                    //returns this response when the api does not return a response with the global stop id
                    return input.getResponseBuilder().withSpeech("Sorry, I couldn't find a stop by that stop code." +
                            " Please try again or provide a different code.").withShouldEndSession(false).build();
                }
            } else {
                return input.getResponseBuilder().withSpeech("Sorry, but I need a stop code to proceed. " +
                        "Let's try that again.").build();
            }
        }
    }

    static class SetAsDefault implements IntentRequestHandler {

        @Override
        public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
            return input.matches(Predicates.intentName("setAsDefaultIntent"));
        }

        @Override
        public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
            AttributesManager attributesManager = input.getAttributesManager();
            Map<String, Object> persistentAttributes = new HashMap<>();
            if (defaultStop != null) {
                persistentAttributes.put("defaultStop", defaultStop);
                attributesManager.setPersistentAttributes(persistentAttributes);
                attributesManager.savePersistentAttributes();
                return input.getResponseBuilder().withSpeech("Stop has been set as default!").build();
            }
            return input.getResponseBuilder().withSpeech("Sorry, but you need to provide a stop code.").build();
        }

    }

}
