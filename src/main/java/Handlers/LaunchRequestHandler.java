package Handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.response.ResponseBuilder;

import java.util.*;

public class LaunchRequestHandler implements com.amazon.ask.dispatcher.request.handler.impl.LaunchRequestHandler { //LaunchRequest occurs when the skill is invoked without a specific intent

    @Override
    public boolean canHandle(HandlerInput input, LaunchRequest launchRequest) {
        return input.matches(Predicates.requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput, LaunchRequest launchRequest) {
        return getActionsResponse();
    }

    //Returns a response with the list of actions a user can perform
    private Optional<Response> getActionsResponse() {
        String actions = "Welcome to Transit Tracker! You can say, search for stops near me, search stop by stop code," +
                " check my stop, or help me. How can I assist you today?";
        ResponseBuilder response = new ResponseBuilder();
        return response.withSpeech(actions).withShouldEndSession(false).build();
    }
}
