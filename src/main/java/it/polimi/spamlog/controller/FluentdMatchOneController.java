package it.polimi.spamlog.controller;

import org.fluentd.logger.FluentLogger;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/fluentd")
public class FluentdMatchOneController {


    private static final org.slf4j.Logger Logger =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    private static FluentLogger LOG = FluentLogger.getLogger("matchone.test");


    @GetMapping
    @RequestMapping("/matchone")
    public ResponseEntity<String> getDati(@RequestParam String RCR)
    {
       Logger.info("ciao fluent");
        MDC.put("dest", "journal");

        Map<String, Object> data = new HashMap<>();
        data.put("from", "userA");
        data.put("to", "userB");
        LOG.log("follow", data);


        return new ResponseEntity<>("ok", HttpStatus.OK);

    }
}
