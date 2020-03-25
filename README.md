 - Progetto Fluentd 
 
 
 - Docker compose file for setting up a EFK service
 - ================================================
 
 Elasticsearch, Fluentd, and Kibana
 
 Example
 -------
 
 
 
 Fatta partire docker
 
 # docker-compose -f docker-compose.yml -f httpd/httpd.yml up
 
 e aggiunta alla configurazione di fluentd
 

 

 
 docker-compose -f docker-compose.yml -f httpd/httpd.yml down
 
 
 curl -X GET http://localhost:9880/fluentd/
 
 
  
 CONTAINER ID        IMAGE                                                 COMMAND                  CREATED              STATUS              PORTS                                                                                  NAMES
 
 x        httpd:2.2.32                                          "httpd-foreground"       About a minute ago   Up About a minute   0.0.0.0:80->80/tcp                                                                     dockercomposeefkmaster_web_1
 x        dockercomposeefkmaster_fluentd                        "tini -- /bin/entryp…"   About a minute ago   Up About a minute   0.0.0.0:9880->9880/tcp, 5140/tcp, 0.0.0.0:24224->24224/tcp, 0.0.0.0:24224->24224/udp   dockercomposeefkmaster_fluentd_1
 x        kibana:7.2.0                                          "/usr/local/bin/kiba…"   27 hours ago         Up About a minute   0.0.0.0:5601->5601/tcp                                                                 dockercomposeefkmaster_kibana_1
 x        docker.elastic.co/elasticsearch/elasticsearch:7.2.0   "/usr/local/bin/dock…"   27 hours ago         Up About a minute   0.0.0.0:9200->9200/tcp, 9300/tcp                                                       dockercomposeefkmaster_elasticsearch_1
 
 
 
http://localhost:5601/app/kibana
 
 Fatta partire applicazione Spring Boot
   
   
 docker logs esempiocdockerfluentd_fluentd_1 | tail -n 1
 
 
  <filter matchone.**>
    @type record_transformer
    <record>
      host_param "#{Socket.gethostname}"
    </record>
  </filter>
  
  
  curl -X GET http://localhost:8080/fluentd/matchone?param=ciaofluent
  
  2020-03-25 18:37:49 +0000 matchone.test.test: {"param1":"ciaofluent","param2":"Hello fluent","host_param":"2ae529af7aad"}
  
  
  curl -X GET http://localhost:8080/fluentd/matchtwo?p=ciaofluent
  
  2020-03-25 18:39:05 +0000 matchtwo.test.dati: {"param":"ciaofluent"}