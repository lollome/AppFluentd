package it.polimi.spamlog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/fluentd")
public class FluentdMatchTwoController
{
    private static final org.slf4j.Logger LOG =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    //private static FluentLogger LOG = FluentLogger.getLogger("matchtwo.test");

    @GetMapping
    @RequestMapping("/matchtwo")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        String json = "{\"name\":  \"lorenzo\"}";

        Map<String, Object> data = new HashMap<>();


        data.put("log", json);

        LOG.info("data",data);

        return new ResponseEntity<>("ok", HttpStatus.OK);



    }


}
