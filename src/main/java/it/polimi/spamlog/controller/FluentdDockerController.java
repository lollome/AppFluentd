package it.polimi.spamlog.controller;

import it.logging.CustomFluentLogger;
import org.fluentd.logger.FluentLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/fluentd")
public class FluentdDockerController {


    private static final org.slf4j.Logger LOGGER =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    private static FluentLogger LOG = CustomFluentLogger.getLogger("pjd.spamfluentd");



    @GetMapping
    @RequestMapping("/spam")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {

        Map<String, Object> data = new HashMap<>();
        data.put("param1", param);
        data.put("param2", "Sono fluent che scrive!!!");

        LOG.log("log", data);

        LOGGER.info("Sono un log info Slf4j");

        return new ResponseEntity<>("ok", HttpStatus.OK);

    }
}
