package it.polimi.spamlog.controller;

import org.fluentd.logger.FluentLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Controller
@RequestMapping("/fluentd")
public class FluentdMatchTwoController
{

    private static FluentLogger LOG = FluentLogger.getLogger("matchtwo.test");

    private final Logger log = LoggerFactory.getLogger(FluentdMatchTwoController.class);



    @GetMapping
    @RequestMapping("/matchtwo")
    public ResponseEntity<String> getDati(@RequestParam String p)
    {

        String json = null;

        Map<String, Object> data = new HashMap<>();
        data.put("rcr", p);
        LOG.log("parametro",data);

        return new ResponseEntity<>("ok", HttpStatus.OK);



    }


}
