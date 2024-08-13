package Handlers;

import Events.SkillDisabledEvent;
import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.amazon.ask.attributes.persistence.impl.DynamoDbPersistenceAdapter;
import com.amazon.ask.services.ApacheHttpApiClient;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

public class TransitTrackerStreamHandler extends SkillStreamHandler { //Entry point of lambda function

    private static Skill getSkill() {
        return Skills.custom().addRequestHandlers(new LaunchRequestHandler(), new CheckStopIntentHandler(),
                                                    new SearchStopsIntentHandler(), new SetDefaultStopIntentHandler(),
                                                    new SkillDisabledEvent(), new CheckDefaultStopIntentHandler(),
                                                    new FallbackIntentHandler(), new SearchStopByCodeIntentHandler(),
                                                    new SearchStopByCodeIntentHandler.SearchByCode(), new SearchStopByCodeIntentHandler.SetAsDefault(),
                                                    new RepeatStopsIntentHandler(), new CancelIntentHandler(),
                                                    new HelpIntentHandler(), new RepeatStopsIntentHandler(),
                                                    new StopIntentHandler(), new NavigateHomeIntentHandler())
                                                    .withPersistenceAdapter(getPersistenceAdapter())
                                                    .withApiClient(ApacheHttpApiClient.standard())
                                                    .build();
    }
    public TransitTrackerStreamHandler() {
        super(getSkill());
    }

    public static DynamoDbPersistenceAdapter getPersistenceAdapter() {
        //configures and returns a persistence adapter with aws credentials and region
        return DynamoDbPersistenceAdapter.builder().withDynamoDbClient(getAmazonDynamoDB())
                .withTableName("Alexa_table")
                .withPartitionKeyName("userId")
                .build();
    }

    public static AWSCredentialsProvider getAWSCredentials() {
        final String accessKey = System.getenv("Access_Key");
        final String secretKey = System.getenv("Secret_Key");
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        return new AWSStaticCredentialsProvider(credentials);
    }

    public static AmazonDynamoDB getAmazonDynamoDB() {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(getAWSCredentials())
                .withRegion(Regions.US_EAST_1)
                .build();
    }




}

