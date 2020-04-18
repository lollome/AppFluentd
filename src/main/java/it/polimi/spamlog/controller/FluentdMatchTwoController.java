package it.polimi.spamlog.controller;

import it.logging.CustomFluentLogger;
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

    private static FluentLogger LOG = CustomFluentLogger.getLogger("pjd.controller");

    @GetMapping
    @RequestMapping("/controller")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        String json = "{\"level\":  \"info log -> debug\"}";

        Map<String, Object> data = new HashMap<>();
        data.put("data", json);

        LOG.log("log",data);

        logger.debug("Questo è un log debug Slf4j");


        return new ResponseEntity<>("ok", HttpStatus.OK);



    }


}
