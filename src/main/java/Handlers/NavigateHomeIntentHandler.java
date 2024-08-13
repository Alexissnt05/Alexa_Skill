package Handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.Optional;

public class NavigateHomeIntentHandler implements IntentRequestHandler {

    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("AMAZON.NavigateHomeIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        return input.getResponseBuilder().withSpeech("Welcome to Transit Tracker! You can say, search for stops near me, search stop by stop code," +
                " check my stop, or help me. How can I assist you today?").withShouldEndSession(false).build();
    }
}
