package it.polimi.spamlog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerFluentd implements CommandLineRunner
{
    private static final Logger logger = LoggerFactory.getLogger(ServerFluentd.class);

    @Autowired
    DisponibilitaService disponibilitaService;
    @Autowired
    private ClientConfig clientConfig;

    @Override
    public void run(String... args)
    {
        logger.info(args[0]);

        String path = args[0] !=null && "disp".equals(args[0]) ? clientConfig.getPathDisponibilita() : clientConfig.getUrldispo();

        //disponibilitaService.getDisponibilita(path);

    }



    public static void main(String[] args)
    {
        SpringApplication.run(ServerFluentd.class, args);
    }
}
