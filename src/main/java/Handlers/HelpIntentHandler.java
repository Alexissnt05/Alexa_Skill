package Handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.IntentRequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.Optional;

public class HelpIntentHandler implements IntentRequestHandler {


    @Override
    public boolean canHandle(HandlerInput input, IntentRequest intentRequest) {
        return input.matches(Predicates.intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, IntentRequest intentRequest) {
        return input.getResponseBuilder().withSpeech("These are the list of commands you can say. " +
                "To search for stops nearby, you can say, search for stops near me, search stops nearby, search stops, or, " +
                "search stops around me. If you would like to check a stop with a stop code, you can say, search stop by stop code, " +
                        "or, search stop by code. If you would like to access" +
                " your default stop, you can say, check stop, check my favorite stop, or, check my default stop. What would you like" +
                        " to do?")
                .withShouldEndSession(false).build();
    }
}
