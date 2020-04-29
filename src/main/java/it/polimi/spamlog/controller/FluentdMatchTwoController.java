package it.polimi.spamlog.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/fluentd")
public class FluentdMatchTwoController
{
    private static final org.slf4j.Logger logger =  org.slf4j.LoggerFactory.getLogger(FluentdMatchOneController.class);

    
    @GetMapping
    @RequestMapping("/controller")
    public ResponseEntity<String> getDati(@RequestParam String param)
    {
        logger.debug("Questo è un log debug Slf4j ");


        return new ResponseEntity<>("ok", HttpStatus.OK);



    }


}
