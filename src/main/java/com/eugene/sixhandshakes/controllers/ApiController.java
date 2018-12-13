package com.eugene.sixhandshakes.controllers;

import com.eugene.sixhandshakes.controllers.responses.BaseResponse;
import com.eugene.sixhandshakes.controllers.responses.ErrorResponse;
import com.eugene.sixhandshakes.controllers.responses.ResultResponse;
import com.eugene.sixhandshakes.controllers.responses.SuccessResponse;
import com.eugene.sixhandshakes.model.App;
import com.eugene.sixhandshakes.model.entities.User;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
    public SuccessResponse postUsers(@RequestBody HashMap<String, User> users){
        User source = users.get("source"), target = users.get("target");
        if (source.isEmpty() || target.isEmpty()) throw new HttpMessageNotReadableException("Missing arguments");
        app.addUsers(source, target);
        return new SuccessResponse(source.getId(), target.getId());
    }

    @GetMapping("/users/result")
    public BaseResponse getResult(@RequestParam(value = "first_id") int firstId,
                                  @RequestParam(value = "second_id") int secondId){

        Document result = app.result(firstId, secondId);

        return !result.isEmpty() ? new ResultResponse(result) :
                new ErrorResponse("Could not find requested pair", 405);

    }
}
