package com.eugene.sixhandshakes.model;

import com.eugene.sixhandshakes.model.entities.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.IOException;
import java.util.HashMap;

import static com.mongodb.client.model.Filters.*;


public class Db {

    private MongoCollection<Document> users, results;
    private ObjectMapper mapper;

    public Db(MongoCollection<Document> users, MongoCollection<Document> results) {
        this.users = users;
        this.results = results;
        mapper = new ObjectMapper();
    }

    public void insertUsers(User source, User target){
        try {
            users.insertOne(
                    new Document(
                            "source", Document.parse(mapper.writeValueAsString(source))
                    ).append(
                            "target", Document.parse(mapper.writeValueAsString(target))
                    )
            );
        } catch (JsonProcessingException e) {
            System.out.println("Unable to insert");
        }
    }

    public HashMap<String, User> nextUsers(){
        Document doc = users.find().first();
        HashMap<String, User> pair = new HashMap<>();
        if (doc!=null){
            try {
                pair.put("source", mapper.readValue(((Document)doc.get("source")).toJson(), User.class));
                pair.put("target", mapper.readValue(((Document)doc.get("target")).toJson(), User.class));
            } catch (IOException e) {
                System.out.println("Unable to read value");
            }
        }
        return pair;
    }

    public void writeResult(User source, User target, int count){
        try {
            results.insertOne(
                    new Document(
                            "source", Document.parse(mapper.writeValueAsString(source))
                    ).append(
                            "target", Document.parse(mapper.writeValueAsString(target))
                    ).append(
                            "count", count
                    )
            );
            users.deleteMany(
                    and(
                            eq("source.id", source.getId()),
                            eq("target.id", target.getId())
                    )
            );
        } catch (JsonProcessingException e) {
            System.out.println("Unable to insert");
        }
    }

    public Document result(int firstId, int secondId){
        Document result = results.find(
                or(
                        and(
                                eq("source.id", firstId),
                                eq("target.id", secondId)
                        ),

                        and(
                                eq("source.id", secondId),
                                eq("target.id", firstId)
                        )
                )
        ).first();

        if (result != null){
            result.remove("_id");
            return result;
        }

        return new Document();
    }
}
