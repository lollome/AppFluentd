package it.polimi.spamlog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


public class ClientConfig
{

    @Value("${http.url_disponibilita}")
    private String urldispo;

    @Value("${http.username}")
    private String httpUsername;

    @Value("${http.password}")
    private String httpPassword;

    @Value("${disponibilita.path}")
    private String pathDisponibilita;


    public String getHttpUsername() {
        return httpUsername;
    }

    public void setHttpUsername(String httpUsername) {
        this.httpUsername = httpUsername;
    }

    public String getUrldispo() {
        return urldispo;
    }

    public void setUrldispo(String urldispo) {
        this.urldispo = urldispo;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }

    public String getPathDisponibilita() {
        return pathDisponibilita;
    }

    public void setPathDisponibilita(String pathDisponibilita) {
        this.pathDisponibilita = pathDisponibilita;
    }
}
