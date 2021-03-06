package it.polimi.spamlog.controller;

import org.fluentd.logger.FluentLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/fluentd")
public class FluentdMatchOneController {


    private static final org.slf4j.Logger logger =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    private static FluentLogger LOG = FluentLogger.getLogger("pjd.spamfluentd");


    @GetMapping
    @RequestMapping("/matchone")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        Map<String, Object> data = new HashMap<>();
        data.put("param1", param);
        data.put("param2", "Hello fluent");

        LOG.log("log", data);


        return new ResponseEntity<>("ok", HttpStatus.OK);

    }
}
