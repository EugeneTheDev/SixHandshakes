package com.eugene.sixhandshakes.model;

import com.eugene.sixhandshakes.model.entities.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;


public class Db {

    private MongoCollection<Document> users, results, averageCountCollection;
    private ObjectMapper mapper;

    public Db(MongoCollection<Document> users, MongoCollection<Document> results,
              MongoCollection<Document> averageCountCollection) {
        this.users = users;
        this.results = results;
        this.averageCountCollection = averageCountCollection;
        mapper = new ObjectMapper();
    }

    public boolean insertUsers(User source, User target){
        try {
            Document doc = users.find(
                    or(
                            and(
                                    eq("source.id", source.getId()),
                                    eq("target.id", target.getId())
                            ),
                            and(
                                    eq("source.id", target.getId()),
                                    eq("target.id", source.getId())
                            )
                    )
            ).first();

            if (doc != null) return false;

            doc = results.find(
                    or(
                            and(
                                    eq("source.id", source.getId()),
                                    eq("target.id", target.getId())
                            ),
                            and(
                                    eq("source.id", target.getId()),
                                    eq("target.id", source.getId())
                            )
                    )
            ).first();

            if (doc != null) return false;

            users.insertOne(
                    new Document(
                            "source", Document.parse(mapper.writeValueAsString(source))
                    ).append(
                            "target", Document.parse(mapper.writeValueAsString(target))
                    )
            );

            return true;

        } catch (JsonProcessingException e) {
            System.out.println("Unable to insert");
        }

        return false;
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

            double averageCount = averageCount();
            long resultsCount = results.countDocuments();
            averageCountCollection.updateOne(
                    exists("averageCount"),
                    set("averageCount", (averageCount*(resultsCount-1) + count)/resultsCount)
            );

        } catch (JsonProcessingException e) {
            System.out.println("Unable to insert");
        }
    }

    public List<Document> result(int userId){
        List<Document> result = results.find(
                or(
                        eq("source.id", userId),
                        eq("target.id", userId)
                )
        ).into(new ArrayList<>());

        if (result != null && !result.isEmpty()){
            result.forEach(e -> e.remove("_id"));
            return result;
        }

        return new ArrayList<>();
    }

    public long usersCount(){
        return users.countDocuments();
    }

    public long resultsCount(){
        return results.countDocuments();
    }

    public double averageCount(){
        return averageCountCollection.find()
                .first()
                .getDouble("averageCount");
    }
}
