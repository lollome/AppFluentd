package it.polimi.spamlog.controller;

import it.logging.CustomFluentLogger;
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

    private static FluentLogger LOG = CustomFluentLogger.getLogger("pjd.view");


    @GetMapping
    @RequestMapping("/view")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        Map<String, Object> data = new HashMap<>();
        data.put("param1", param);
        data.put("param2", "Spam Fluent");

        LOG.log("log", data);

        logger.info( "Questo è un log info Slf4j: " + param);
        logger.debug("Questo è un log debug Slf4j");

        return new ResponseEntity<>("ok", HttpStatus.OK);

    }
}
