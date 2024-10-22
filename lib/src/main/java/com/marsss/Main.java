package com.marsss;

import com.marsss.callerphone.Callerphone;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.*;
import org.bson.Document;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static com.mongodb.client.model.Filters.eq;

public class Main {
    public static void main(String[]args) throws InterruptedException, UnsupportedEncodingException, URISyntaxException {
        Callerphone.run();
    }
}
