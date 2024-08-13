package Events;

import Handlers.TransitTrackerStreamHandler;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.impl.SkillDisabledRequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.events.skillevents.SkillDisabledRequest;
import com.amazon.ask.request.Predicates;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Class that handles the event when a user disables the skill on their device
public class SkillDisabledEvent implements SkillDisabledRequestHandler {
    @Override
    public boolean canHandle(HandlerInput input, SkillDisabledRequest skillDisabledRequest) {
        return input.matches(Predicates.requestType(SkillDisabledRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input, SkillDisabledRequest skillDisabledRequest) {
        String userId = input.getRequestEnvelope().getContext().getSystem().getUser().getUserId();
        AmazonDynamoDB dynamoDB = TransitTrackerStreamHandler.getAmazonDynamoDB();
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("userId", new AttributeValue(userId));
        DeleteItemRequest request = new DeleteItemRequest()
                .withTableName("Alexa_table")
                .withKey(key);
        dynamoDB.deleteItem(request); //Deletes the item from DynamoDB table when a user disables the skill.
        return input.getResponseBuilder().withSpeech("Skill has been disabled").build();
    }
}
