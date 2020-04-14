package it.polimi.spamlog.controller;

import org.fluentd.logger.FluentLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/fluentd")
public class FluentdMatchTwoController
{
    private static final org.slf4j.Logger logger =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    private static FluentLogger LOG = FluentLogger.getLogger("pjd.spamfluentd");

    @GetMapping
    @RequestMapping("/matchtwo")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        String json = "{\"level\":  \"debug\"}";

        Map<String, Object> data = new HashMap<>();


        data.put("log", json);

        LOG.log("logger",data);
        logger.debug(" allora lo scriviamo questo log?");


        return new ResponseEntity<>("ok", HttpStatus.OK);



    }


}
