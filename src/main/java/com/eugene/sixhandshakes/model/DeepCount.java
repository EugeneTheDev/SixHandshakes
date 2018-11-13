package com.eugene.sixhandshakes.model;

import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ApiTooManyException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component()
public class DeepCount {

    private VkApiClient vk;
    private UserActor owner;
    private AtomicInteger countOfInteractions;
    private ExecutorService service;

    public DeepCount() {
        initApp();
    }

    private void initApp(){

        Properties properties = new Properties();
        try {
            properties.load(DeepCount.class.getClassLoader().getResourceAsStream("static/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        vk=new VkApiClient(new HttpTransportClient());
        owner=new UserActor(Integer.valueOf(properties.getProperty("owner-id")),
                properties.getProperty("owner-access-token"));

        countOfInteractions = new AtomicInteger(0);
        service = Executors.newFixedThreadPool(3);
        startCheckThread();

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

    public void findTargetAndCount(User source, User target){
        service.submit(()->{
            LinkedList<Long> users = new LinkedList<>();
            int result = -1;
            int sourceId = source.getId(), targetId = target.getId();
            try {
                List<Integer> buffer;
                int i=0;
                do {

                    check();
                    buffer = vk.friends()
                            .get(owner)
                            .count(4000)
                            .offset(i)
                            .userId(sourceId)
                            .execute()
                            .getItems();
                    i+=4000;

                    for (Integer user: buffer) if (user.equals(targetId)){
                        result = 1;
                        break;
                    }

                } while(!buffer.isEmpty());

            } catch (ApiException | ClientException ignore) {
                System.out.println("Account is private");
            }

            users.offer(Long.valueOf(Integer.toString(sourceId) + 0));
            while (result<=0){

                result = findTargetAndCount(users, targetId);

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignore) {}

            }

            System.out.printf("%s <-%d-> %s", source.getFirstName(), result, target.getFirstName());
        });

    }

}