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
 

   
   # Dockerizzazione dell'applicazione
   
   1.  creazione del jar
       
            $ ./gradlew build
    
   2. Preparazione delle dipendenze per l'applicazione per quando girerà sulla docker
    
            $ mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)
   
   
   3. Build della Image
   
            $ docker build  (--build-arg DEPENDENCY=build/dependency) -t appfluentd/spam-fluentd:latest --rm=true .
       
            -t (--tag) seguito da name:versione prepara la image 
            
            
         
         possiamo aggiungere esplicitamente il paramametro che è pue valorizzando demtro il Dockerfile
         
            --build-arg DEPENDENCY=build/dependency
       
   infatti se eseguiamo il comando 
        
    $ docker images 
    
   vedremo come è memorizzata in docker
   
        REPOSITORY                                      TAG                 IMAGE ID            CREATED             SIZE
        appfluentd/spam-fluentd                         latest              fefedca30972        15 seconds ago      137MB
       
       
   4. Per ora non abbiamo un server dove registriamo la nosta image. Eseguimo localmente la nostra appplicazione
      
            $ docker run -p 8080:8080 -t appfluentd/spam-fluentd
            
         vedremo la nostra app in background; per vedere i log
            
            $ docker ps -a

            CONTAINER ID        IMAGE                                                 COMMAND                  CREATED             STATUS                       PORTS               NAMES
            9b36109e3110        appfluentd/spam-fluentd                               "java -cp app:app/li…"   2 minutes ago       Exited (1) 2 minutes ago                         agitated_sammet


            $ docker logs 9b36109e3110 | tail -n 10
             
             (docker logs container_id | tail -n 10)
   
 

 
   5. Registrazione di una docker
   
        le images docker devono essere rese disponibili per poter essere usate da + parti
        e per poter far ciò avremmo di un repository registry; noi non ne abbiamo uno per cui dockerizziamo un registry
    
        1. Creare il registry su minikube (dal file yml)
                
                $ kubectl create -f kube-registry.yaml
                
              possiamo visualizzare i pods e vedere specificatamente il kube-registry-v0-xxxx creato; 
                
                $ kubectl get po -n kube-system | grep kube-registry-v0 | \awk '{print $1;}'
                
              in caso non sia in stato di Running (tipo ImagePullBackOff) è possibile vedere possibili errori con il comando 
                                
                $ kubectl -n kube-system describe pod kube-registry-v0-xxxxx
                
                se andato tutto bene facciamo il forward -> punto 2
        
        2. Forward della porta 5000 da localhost a minikube
    
                $ kubectl port-forward -n kube-system kube-registry-v0-b2n5w 5000:5000
    
              N.B: il nome del pod varia ogni volta.
    
        3. Build, tag e push
    
    
                $ docker build -t localhost:5000/appfluentd/spam-fluentd:latest .
                $ docker push localhost:5000/appfluentd/spam-fluentd:latest
           
    
        4. Ora è disponibile su un registry ed è possibile usarla da parte terzi
        
        
        
    
    