package com.mulesoft.erpanderp;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.Message;

import org.json.JSONObject;
public class TrySNS {

	public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException, IOException
	{
		File fSNS = new File("/home/java/.aws/snsCreds");
		AWSCredentials credentials = new PropertiesCredentials(fSNS);
		AmazonSNSClient snsClient = new AmazonSNSClient(credentials);		                           
		snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));

		//create a new SNS topic
		CreateTopicRequest createTopicRequest = new CreateTopicRequest("MyNewTopic"+UUID.randomUUID());
		CreateTopicResult createTopicResult = snsClient.createTopic(createTopicRequest);
		String topicArn = createTopicResult.getTopicArn();
		
		File fSQS = new File("/home/java/.aws/sqsCreds");
		AmazonSQS sqsClient = new AmazonSQSClient(new PropertiesCredentials(fSQS));
        Region usEast2 = Region.getRegion(Regions.US_EAST_1);
        sqsClient.setRegion(usEast2);

        // Create a queue
        System.out.println("Creating a new SQS queue called MyQueue.\n");
        CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue"+UUID.randomUUID().toString());
        CreateQueueResult cqResult = sqsClient.createQueue(createQueueRequest);

		//Get the queue ARN
		List<String> attribs = new ArrayList<String>();
		attribs.add("QueueArn");
		GetQueueAttributesRequest qaRequest = new GetQueueAttributesRequest(cqResult.getQueueUrl(), attribs);
		GetQueueAttributesResult qaResult = sqsClient.getQueueAttributes(qaRequest);
		String queueArn = qaResult.getAttributes().get("QueueArn");
		
		//subscribe to an SNS topic
		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "sqs", queueArn);
		SubscribeResult subResult = snsClient.subscribe(subRequest);
		System.out.println(subResult.toString());
		
		//Receive the confirmation message
		ReceiveMessageRequest rmRequest = new ReceiveMessageRequest(cqResult.getQueueUrl());
		rmRequest.setWaitTimeSeconds(10);
		ReceiveMessageResult rmResult = sqsClient.receiveMessage(rmRequest);
		Message msg  = rmResult.getMessages().get(0);
		
		JSONObject object = new JSONObject(msg);
        String token = (String) object.get("Token");
        System.out.println("receiveConfirmation token is "+ token);

		Scanner in = new Scanner(System.in);
		int num = in.nextInt();
		//int done = Integer.parseInt(System.console().readLine());
		System.out.println("Waiting over.");
		
		//publish to an SNS topic
//		String msg = "My text published to SNS topic with email endpoint";
//		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
//		PublishResult publishResult = snsClient.publish(publishRequest);
//		//print MessageId of message published to SNS topic
//		System.out.println("MessageId - " + publishResult.getMessageId());
	}
}
