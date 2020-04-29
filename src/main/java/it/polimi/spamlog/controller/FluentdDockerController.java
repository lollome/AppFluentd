package it.polimi.spamlog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/fluentd")
public class FluentdDockerController {


    private static final org.slf4j.Logger LOGGER =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);


    @GetMapping
    @RequestMapping("/spam")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {
    	LOGGER.info("Sono un log info Slf4j " + param);

        return new ResponseEntity<>("ok", HttpStatus.OK);

    }
}
