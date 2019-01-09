package com.eugene.sixhandshakes.model;

import com.eugene.sixhandshakes.controllers.responses.ResultResponse;
import com.eugene.sixhandshakes.controllers.responses.SuccessResponse;
import com.eugene.sixhandshakes.controllers.responses.UpdateResponse;
import com.eugene.sixhandshakes.model.entities.User;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.users.UserField;
import org.bson.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class App {

    private VkApiClient vk;
    private UserActor owner;
    private AtomicInteger countOfInteractions;
    private Db db;

    public App() {
        initApp();
    }

    private void initApp(){

        Properties properties = new Properties();
        try {
            properties.load(App.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        vk=new VkApiClient(new HttpTransportClient());
        owner=new UserActor(Integer.valueOf(properties.getProperty("owner-id")),
                properties.getProperty("owner-access-token"));

        initDb(properties);

        countOfInteractions = new AtomicInteger(0);
        startCheckThread();
        startChecking();

    }

    private void initDb(Properties properties){
        MongoClientURI uri  = new MongoClientURI(String.format("mongodb://%s:%s@%s:%s/%s",
                properties.getProperty("db-user"), properties.getProperty("db-password"),
                properties.getProperty("db-host"), properties.getProperty("db-port"),
                properties.getProperty("db-name")));
        MongoClient client = new MongoClient(uri);
        db = new Db(client.getDatabase(uri.getDatabase()).getCollection(properties.getProperty("db-users")),
                client.getDatabase(uri.getDatabase()).getCollection(properties.getProperty("db-results")),
                client.getDatabase(uri.getDatabase()).getCollection(properties.getProperty("db-average-count")));
    }

    private void startCheckThread(){
        Thread thread=new Thread(()->{
            while (Thread.currentThread().isAlive()) {
                try {
                    Thread.sleep(1000);
                    countOfInteractions.set(0);
                } catch (InterruptedException ignore) {}
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void check(){
        while (countOfInteractions.get()>=3) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        countOfInteractions.getAndIncrement();
    }

    private int findTargetAndCount(LinkedList<Long> queue, int targetId){
        Long current = queue.poll();

        int count = Integer.valueOf(current.toString().substring(current.toString().length()-1)),
                sourceId = Integer.valueOf(current.toString().substring(0, current.toString().length()-1));

        System.out.println("Now serving " + sourceId + " with count " + count);
        List<Integer> friends = new ArrayList<>(), buffer;

        boolean isRepeat = true;
        while (isRepeat) {
            try {

                isRepeat = false;
                synchronized (vk) {
                    if(!vk.friends()
                            .getMutual(owner)
                            .sourceUid(sourceId)
                            .targetUid(targetId)
                            .execute().isEmpty()) return count+2;

                    int i=0;
                    do {

                        buffer = vk.friends()
                                .get(owner)
                                .count(4000)
                                .offset(i)
                                .userId(sourceId)
                                .execute()
                                .getItems();
                        friends.addAll(buffer);
                        i+=4000;

                    } while(!buffer.isEmpty());
                }

                for (Integer friend: friends){
                    String s = friend.toString() + (count + 1);
                    queue.offer(Long.valueOf(s));
                }

            } catch (ApiTooManyException e){
                isRepeat = true;

            } catch (ApiException | ClientException e) {
                System.out.println("Account is private");
            }
        }

        return -1;
    }

    private void startChecking(){
        new Thread(()->{
            while (Thread.currentThread().isAlive()) {
                HashMap<String, User> pair = db.nextUsers();
                if (pair.isEmpty()) continue;
                User source = pair.get("source"), target = pair.get("target");

                LinkedList<Long> users = new LinkedList<>();
                int result = -1;
                int sourceId = source.getId(), targetId = target.getId();
                try {
                    List<Integer> buffer;
                    int i = 0;
                    do {

                        check();

                        synchronized (vk) {
                            buffer = vk.friends()
                                    .get(owner)
                                    .count(4000)
                                    .offset(i)
                                    .userId(sourceId)
                                    .execute()
                                    .getItems();
                        }

                        i += 4000;

                        for (Integer user : buffer)
                            if (user.equals(targetId)) {
                                result = 1;
                                break;
                            }

                    } while (!buffer.isEmpty());

                } catch (ApiException | ClientException ignore) {
                    System.out.println("Account is private");
                }

                users.offer(Long.valueOf(Integer.toString(sourceId) + 0));
                while (result <= 0) {

                    result = findTargetAndCount(users, targetId);

                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignore) {
                    }

                }

                db.writeResult(source, target, result);

            }
        }).start();

    }

    public SuccessResponse insertUsers(String sourceId, String targetId) throws ClientException, ApiException {
        if (sourceId.equals(targetId)) throw new ClientException("Users must be different");

        check();
        UserXtrCounters sourceCounters, targetCounters;
        boolean isPrivate;

        synchronized (vk) {

            List<UserXtrCounters> users = vk.users()
                    .get(owner)
                    .fields(UserField.FOLLOWERS_COUNT)
                    .userIds(sourceId, targetId)
                    .execute();
            targetCounters = users.get(1);
            sourceCounters = users.get(0);

            check();
            isPrivate = vk.execute()
                    .code(owner, String.format(
                            "var sourceFollowers = API.friends.get({\"user_id\": %d, \"count\": 1});\n" +
                            "var targetFollowers = API.friends.get({\"user_id\": %d, \"count\": 1});\n" +
                            "if (!sourceFollowers || !targetFollowers) return null;\n" +
                            "return {\"success\": true};",
                            sourceCounters.getId(), targetCounters.getId()))
                    .execute()
                    .isJsonNull();

        }

        if (isPrivate) throw new ClientException("One or both of accounts is private or friends list is hidden");

        User source = new User(sourceCounters.getFirstName(), sourceCounters.getLastName(), sourceCounters.getId()),
                target = new User(targetCounters.getFirstName(), targetCounters.getLastName(), targetCounters.getId());

        if (!db.insertUsers(source, target)) throw new ClientException("This pair already exists in database");

        return new SuccessResponse(source.getId(), target.getId());
    }

    public ResultResponse<List<Document>> result(String userId) throws IllegalArgumentException, ClientException, ApiException {
        check();
        UserXtrCounters user;

        synchronized (vk) {
            user = vk.users()
                    .get(owner)
                    .userIds(userId)
                    .execute()
                    .get(0);
        }

        List<Document> result = db.result(user.getId());
        if (result.isEmpty()) throw new IllegalArgumentException("Cannot find requested user");
        return new ResultResponse<>(result);
    }

    public UpdateResponse update(){
        return new UpdateResponse(db.resultsCount(),db.usersCount(), db.averageCount());
    }

}
