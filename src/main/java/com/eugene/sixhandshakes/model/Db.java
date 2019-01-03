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

            if (doc != null) return;

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

            if (doc != null) return;

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
}
