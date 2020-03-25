 - Progetto Fluentd 
 
   L'applicazione Spring logga con libreria fluentd 
   
   Fluentd -> Elasticsearch -> Kibana
 
 
 - EFK è installaro tramite Docker

 
 
 # Installazione docker Elasticsearch, Fluentd, and Kibana
                      
 
 ### UP DOCKER 
 
    docker-compose -f docker-compose.yml -f httpd/httpd.yml up
 

 ### DOWN DOCKER

    docker-compose -f docker-compose.yml -f httpd/httpd.yml down
 
 
 # Container instanziati
    
    CONTAINER ID        IMAGE                                                 COMMAND                  CREATED              STATUS              PORTS                                                                                  NAMES
     
     x        httpd:2.2.32                                          "httpd-foreground"       About a minute ago   Up About a minute   0.0.0.0:80->80/tcp                                                                     dockercomposeefkmaster_web_1
     x        dockercomposeefkmaster_fluentd                        "tini -- /bin/entryp…"   About a minute ago   Up About a minute   0.0.0.0:9880->9880/tcp, 5140/tcp, 0.0.0.0:24224->24224/tcp, 0.0.0.0:24224->24224/udp   dockercomposeefkmaster_fluentd_1
     x        kibana:7.2.0                                          "/usr/local/bin/kiba…"   27 hours ago         Up About a minute   0.0.0.0:5601->5601/tcp                                                                 dockercomposeefkmaster_kibana_1
     x        docker.elastic.co/elasticsearch/elasticsearch:7.2.0   "/usr/local/bin/dock…"   27 hours ago         Up About a minute   0.0.0.0:9200->9200/tcp, 9300/tcp                                                       dockercomposeefkmaster_elasticsearch_1
        
 # KIBANA
    
    
    http://localhost:5601/app/kibana
    

 # TEST
 
 Dopo aver fatto partire le docker, far partire l'applicazione
 
 Ho fatto due controller
 
    1) it.polimi.spamlog.FluentdMatchOneController
    
   con log fluentd
   
    private static FluentLogger LOG = FluentLogger.getLogger("matchone.test"); 
    
    
    1) it.polimi.spamlog.FluentdMatchTwoController
    
   con log fluentd
   
    private static FluentLogger LOG = FluentLogger.getLogger("matchtwo.test"); 
    
 
  da configurazione in  fluentd.conf
  
  il log vengono prima filtrati
 
    <filter matchone.**>
     @type record_transformer
     <record>
       host_param "#{Socket.gethostname}"
     </record>
    </filter>
   
 
 poi inviati nei due store del match 
 - elasticsearch
 - stdout
 
 
 <match *.**>
   @type copy
   <store>
     @type elasticsearch
     host elasticsearch
     port 9200
     logstash_format true
     logstash_prefix fluentd
     logstash_dateformat %Y%m%d
     include_tag_key true
     type_name access_log
     tag_key @log_name
     flush_interval 1s
   </store>
   <store>
     @type stdout
   </store>
 </match>
 
 chiamo i due controller con i conseguenti log che si posso osservare
 
 
    curl -X GET http://localhost:8080/fluentd/matchone?param=ciaofluent
   
    2020-03-25 18:37:49 +0000 matchone.test.test: {"param1":"ciaofluent","param2":"Hello fluent","host_param":"2ae529af7aad"}
   
   
    curl -X GET http://localhost:8080/fluentd/matchtwo?p=ciaofluent
   
    2020-03-25 18:39:05 +0000 matchtwo.test.dati: {"param":"ciaofluent"}
    
    
    param:ciaofluent 
    @timestamp:Mar 25, 2020 @ 22:08:58.000 
    @log_name:matchtwo.test.dati 
    _id:UuyGE3EBqkgChWXa37p3 
    _type:access_log 
    _index:fluentd-20200325 _score: -
 
  
    nel primo a causa del filter viene aggiunto host_param
 

   
   
 docker logs esempiocdockerfluentd_fluentd_1 | tail -n 1
 
 
  
  
  