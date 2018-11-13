package com.eugene.sixhandshakes.controllers;

import com.eugene.sixhandshakes.model.DeepCount;
import com.eugene.sixhandshakes.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class Controller {

    private final DeepCount deepCount;

    @Autowired
    public Controller(DeepCount deepCount) {
        this.deepCount = deepCount;
    }

    @PostMapping
    public String postUsers(@RequestBody HashMap<String, User> users){
        deepCount.findTargetAndCount(users.get("source"), users.get("target"));
        return  String.format("%s <-> %s", users.get("source").getFirstName(), users.get("target").getFirstName());
    }
}
