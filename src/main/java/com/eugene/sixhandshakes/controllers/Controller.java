package com.eugene.sixhandshakes.controllers;

import com.eugene.sixhandshakes.controllers.responses.SuccessResponse;
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

    @PostMapping("/users")
    public SuccessResponse postUsers(@RequestBody HashMap<String, User> users){
        User source = users.get("source"), target = users.get("target");
        deepCount.addUsers(source, target);
        return new SuccessResponse(source.getId(), target.getId());
    }
}
