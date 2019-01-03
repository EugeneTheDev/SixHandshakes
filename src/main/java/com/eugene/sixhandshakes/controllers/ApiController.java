package com.eugene.sixhandshakes.controllers;

import com.eugene.sixhandshakes.controllers.responses.BaseResponse;
import com.eugene.sixhandshakes.controllers.responses.ErrorResponse;
import com.eugene.sixhandshakes.model.App;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final App app;

    @Autowired
    public ApiController(App app) {
        this.app = app;
    }

    @PostMapping("/users/insert")
    public BaseResponse postUsers(@RequestBody HashMap<String, String> users){
        String source = users.get("source"), target = users.get("target");
        try {
            return app.insertUsers(source, target);
        } catch (ClientException | ApiException e) {
            return new ErrorResponse(e.getMessage());
        }
    }

    @GetMapping("/users/result")
    public BaseResponse getResult(@RequestParam(value = "user_id") String userId){
        try{
            return app.result(userId);
        } catch (IllegalArgumentException | ClientException | ApiException e){
            return new ErrorResponse(e.getMessage());
        }

    }
}
