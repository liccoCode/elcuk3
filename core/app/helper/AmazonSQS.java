package helper;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Created by IntelliJ IDEA.
 * User: kenyon
 * Date: 5/15/17
 * Time: 10:23 PM
 */
public class AmazonSQS {

    private AmazonSQS(){
    }

    private static final String SQS_QUEUE = String.format("https://sqs.us-west-2.amazonaws.com/866320605929/%s",
            System.getenv("AWS_SQS_QUEUE"));
    private volatile static AmazonSQSAsyncClient instance;


    private static AmazonSQSAsyncClient cliet() {
        if(instance == null) {
            synchronized(AmazonSQSAsyncClient.class) {
                if(instance == null) {
                    instance = new AmazonSQSAsyncClient();
                    instance.setRegion(Region.getRegion(Regions.US_WEST_2));
                }
            }
        }
        return instance;
    }

    public static void sendMessage(String message) {
        cliet().sendMessage(new SendMessageRequest(SQS_QUEUE, message));
    }
}
