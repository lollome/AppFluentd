 
   
   ## Progetto EFK (Elasticsearch-Fluentd-Kibana )
      
   Lo scopo del progetto è quello di scrivere i log sullo stack EFK (Elasticsearch Fluentd Kibana)
      
   Vogliamo eseguire 2 test
   - 1° TEST - Installazione EFK su docker
   - 2° TEST - Deploy dell'applicazione AppFluentd in un cluster Kubernates creato tramite Minikube (branch efk_kubernates)
      
      
   ### 1° TEST - Installazione EFK su docker
      
   Il primo test che eseguiremo è eseguire l'applicazione AppFluent su ambiente locale la quale scrive i log  su stack EFK installato su docker e 
   

   - Aggiungiamo nel build.gradle la libreria di fluentd
           
       `compile group: 'org.fluentd', name: 'fluent-logger', version: '0.2.11'`
      
      
   usiamo la libreria con le impostazioni di default
   
        `private static FluentLogger LOG = FluentLogger.getLogger("spamfluentd");
        
        
        ossia se esploriamo sotto vediamo che usa come host localhost e porta 24224
        
        public static FluentLogger getLogger(String tagPrefix) {
                return factory.getLogger(tagPrefix, "localhost", 24224);
            }
        
        `
         
   - Installazione docker Elasticsearch, Fluentd, and Kibana
                                
           
    UP DOCKER 
           
        docker-compose -f docker-compose-efk-master/docker-compose.yml up
          
    DOWN DOCKER
       
        docker-compose -f docker-compose.yml -f httpd/httpd.yml down
           
           
   Container instanziati
          
      CONTAINER ID        IMAGE                                                 COMMAND                  CREATED              STATUS              PORTS                                                                                  NAMES
       
       6595bdb8eabd        docker.elastic.co/kibana/kibana-oss:7.6.2                 "/usr/local/bin/dumb…"   14 minutes ago      Up 14 minutes             0.0.0.0:5601->5601/tcp                                                                 dockercomposeefkmaster_kibana_1
       7c63666a0cfb        dockercomposeefkmaster_fluentd                            "tini -- /bin/entryp…"   14 minutes ago      Up 14 minutes             0.0.0.0:9880->9880/tcp, 5140/tcp, 0.0.0.0:24224->24224/tcp, 0.0.0.0:24224->24224/udp   dockercomposeefkmaster_fluentd_1
       81e65a009bd3        docker.elastic.co/elasticsearch/elasticsearch-oss:7.6.2   "/usr/local/bin/dock…"   14 minutes ago      Up 14 minutes             0.0.0.0:9200->9200/tcp, 9300/tcp                                                       dockercomposeefkmaster_elasticsearch_1
              
     
      KIBANA è raggiungibile  [http://localhost:5601/app/kibana]
              
          
   TEST
   
   Configuriamo per ricevere i log sulla porta 24224 e forwardiamo in output sia su stout che Elastichsearch.
   
           
       <match *.**>
         @type copy
         <store>
           @type elasticsearch
           @id out_es
           @log_level info
           include_tag_key true
           host elasticsearch
           port 9200
           logstash_format true
           logstash_prefix 'logstash'
           logstash_format true
           logstash_dateformat %Y%m%d
           type_name access_log_ll
           tag_key @log_name
           flush_interval 1s
         </store>
         <store>
           @type stdout
         </store>
       </match>
       
   chiamiamo i tre controller controller con i conseguenti log che si posso osservare
      
   1° Controller
          
    $ curl -X GET http://localhost:8080/fluentd/matchone?param=ciaofluent
    ok
   
    stdout =>  fluentd_1        | 2020-04-13 12:23:52 +0000 pjd.spamfluentd.log: {"param1":"ciaofluent","param2":"Hello fluent"}
      
   configuriamo Kibana (creiamo l'indice) e possiamo notare il seguente log
   
  
    Time	                        @log_name	            param1	    param2	        message	        log
    Apr 13, 2020 @ 14:23:52.000	pjd.spamfluentd.log	    ciaofluent	Hello fluent	 - 	            - 
          
      
   2° Controller
      
    $ curl -X GET http://localhost:8080/fluentd/matchtwo?param=ciao_fluent
    ok
   
    stout =>    fluentd_1        | 2020-04-13 12:25:22 +0000 pjd.spamfluentd.logger: {"log":"{\"name\":  \"lorenzo\"}"}
      
    Time	                        @log_name	            param1	    param2	        message	        log
    Apr 13, 2020 @ 14:25:22.000	pjd.spamfluentd.logger	 - 	 - 	 - 	{"name":  "lorenzo"}
     
   3° Controller
    
    $ curl -X GET http://localhost:8080/fluentd/docker?param=ciao_fluent 
    ok
   
    fluentd_1        | 2020-04-13 12:27:00 +0000 spamfluentd.log: {"param1":"ciao_fluent","param2":"Hello fluent"}
   
    Time	                        @log_name	            param1	    param2	        message	        log
    Apr 13, 2020 @ 14:27:00.000	spamfluentd.log	ciao_fluent	Hello fluent	 - 	 - 
      
         #### Stack EFK su kubernates
         
         
         deploy su registry la versione fluentd 1.0
         
          $ ./gradlew clean build
          $ mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)
          $ docker build -t localhost:5000/appfluentd/spam-fluentd:1.0 .
          $ docker push localhost:5000/appfluentd/spam-fluentd:1.0
         
         
         modifico in deployament.yaml la image della app
            
          image: localhost:5000/appfluentd/spam-fluentd:1.0
         
         e riapplico tutto (per essere veloce potevo fare solo il deployament)
         
          $ kubectl replace --force -f pod-app/
   
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
    
        a. Creare il registry su minikube (dal file yml)
                
                $ kubectl create -f kube-registry.yaml
                
              possiamo visualizzare i pods e vedere specificatamente il kube-registry-v0-xxxx creato; 
                
                $ kubectl get po -n kube-system | grep kube-registry-v0 | \awk '{print $1;}'
                
              in caso non sia in stato di Running (tipo ImagePullBackOff) è possibile vedere possibili errori con il comando 
                                
                $ kubectl -n kube-system describe pod kube-registry-v0-xxxxx
                
                se andato tutto bene facciamo il forward -> punto 2
        
        b. Forward della porta 5000 da localhost a minikube
    
                $ kubectl port-forward -n kube-system kube-registry-v0-b2n5w 5000:5000
    
              N.B: il nome del pod varia ogni volta.
    
        c. Build, tag e push
        
                $ docker build -t localhost:5000/appfluentd/spam-fluentd:latest .
                $ docker push localhost:5000/appfluentd/spam-fluentd:latest
                                       
            
                Ora è disponibile su un registry ed è possibile usarla come container in un deployement
        
   6. Deploy dell'applicazione in un cluster
   
        Come cluster usiamo minikube e per deployare l'applicazione usiamo Kubernates (k8s)
        
        (per installazione di minikube si rimanda ad altri manuali)
        
        kubernate fornisce un api per la creazione degli oggetti Kubectl
        
        Creazione degli oggetti
        
            $ kubectl create - f pathfileyaml o dir
        
        per cui noi eseguimao 
        
            $ kubectl create -f pod-app/
        
        in ordine creerà tutti gli oggetti descritti nei seguenti file yaml
        
            01-namespace.yaml
            10-secret.yaml
            11-service.yaml
            12-deployment.yaml
            20-ingress.yaml
            
            
            $ kubectl get pods
            
            NAME                                    READY   STATUS    RESTARTS   AGE
            d-spamfluentd-569c47c85c-cn7xk          1/1     Running   0          165m
            
            
            $ kubectl describe pods -n kube-spam
            
            in output è possibile vedere cosa si è creato e lo stato
            
                Name:           d-spamfluentd-569c47c85c-cckr5
                Namespace:      kube-spam
                Priority:       0
                Node:           m01/172.26.0.2
                Start Time:     Mon, 06 Apr 2020 14:18:57 +0200
                Labels:         app=spamfluentd
                                pod-template-hash=569c47c85c
                                release=spamfluentd-1
                Annotations:    <none>
                Status:         Pending/Waiting
                IP:             
                IPs:            <none>
                Controlled By:  ReplicaSet/d-spamfluentd-569c47c85c
                Containers:
                  spamfluentd:
                    Container ID:   
                    Image:          localhost:5000/appfluentd/spam-fluentd:latest
                    Image ID:       
                    Port:           8080/TCP
                    Host Port:      0/TCP
                    State:          Waiting
                      Reason:       ContainerCreating
                    Ready:          False
                    Restart Count:  0
                    Requests:
                      cpu:        100m
                    Environment:  <none>
                    Mounts:
                      /etc/app.d/01-secret/ from secret-app (rw)
                      /var/run/secrets/kubernetes.io/serviceaccount from default-token-9p8k4 (ro)
                Conditions:
                  Type              Status
                  Initialized       True 
                  Ready             False 
                  ContainersReady   False 
                  PodScheduled      True 
                Volumes:
                  secret-app:
                    Type:        Secret (a volume populated by a Secret)
                    SecretName:  app-035002
                    Optional:    false
                  default-token-9p8k4:
                    Type:        Secret (a volume populated by a Secret)
                    SecretName:  default-token-9p8k4
                    Optional:    false
                QoS Class:       Burstable
                Node-Selectors:  <none>
                Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
                                 node.kubernetes.io/unreachable:NoExecute for 300s
                Events:
                  Type     Reason       Age                  From               Message
                  ----     ------       ----                 ----               -------
                  Normal   Scheduled    <unknown>            default-scheduler  Successfully assigned kube-spam/d-spamfluentd-569c47c85c-cckr5 to m01
                  Warning  FailedMount  12m (x4 over 23m)    kubelet, m01       Unable to attach or mount volumes: unmounted volumes=[secret-app], unattached volumes=[default-token-9p8k4 secret-app]: timed out waiting for the condition
                  Warning  FailedMount  10m (x6 over 30m)    kubelet, m01       Unable to attach or mount volumes: unmounted volumes=[secret-app], unattached volumes=[secret-app default-token-9p8k4]: timed out waiting for the condition
                  Warning  FailedMount  118s (x23 over 32m)  kubelet, m01       MountVolume.SetUp failed for volume "secret-app" : secret "app-035002" not found

            
            
            considerando che ogni ooggetto è stato creato sotto namespce kube-spam
            
            $ kubectl get pods -n kube-sapm
            
             
                NAME                             READY   STATUS              RESTARTS   AGE
                d-spamfluentd-569c47c85c-cckr5   0/1     ContainerCreating   0          25m
                
            $ kubectl describe pod d-spamfluentd-569c47c85c-cn7xk
                
            $ kubectl get service -n kube-spam
              
              NAME            TYPE       CLUSTER-IP    EXTERNAL-IP   PORT(S)          AGE
              s-spamfluentd   NodePort   10.96.95.33   <none>        8080:30404/TCP   26m
              
            $ kubectl get deployment nginx-deployment -o yaml
              aiuta a capire lo stato del deployment
            
            
            Il deployment.yaml mi crea il container con la mia image dell'applicazione creata e che scarico dal mio registro locale e un volume dove inserisco una chiave privata
            che ora non mi serve a nulla
            
            Il service.yaml mi dice il servizio che deve essere esposto
            
            Un modo astratto per esporre un'applicazione in esecuzione su un set di pod come servizio di rete.
            
            
            Per esporre il servizio all'eserno ho bisogno dell' ingress.yaml
            
            per esporlo all'esterno ho necissità di altre configurazioni:
            
            - To enable the NGINX Ingress controller, run the following command:
            
                minikube addons enable ingress
            
            
            - Verify that the NGINX Ingress controller is running
            
                    kubectl get pods -n kube-system
            
                    ....
            
                    nginx-ingress-controller-5984b97644-rnkrg   1/1       Running   0          1m
                    
                    .....
                    
               $ kubectl get ingress
               NAME                 HOSTS                      ADDRESS      PORTS     AGE
               webspam-ingress      spamfluentd.info           172.26.0.2   80        43m
               
               Add the following line to the bottom of the /etc/hosts file.
               
               echo "$(minikube ip) spamfluentd.info" | sudo tee -a /etc/hosts
               
               oppure aggiungere a mano nel file /etc/hostes
               
               172.26.0.2 spamfluentd.info
               
               $ curl http://spamfluentd.info/fluentd/matchone?param=ciao
                
                response
                    ok
                    
                oppure da browser 
                
                http://spamfluentd.info/fluentd/matchone?param=ciao

               
               
               
               
    
    




Fluentd
-------

Gli input source di fluentd sono abilitati selezionando e configurando i plugins desiderati e usando le direttive di source

Source invia eventi al motore di routing di Fluentd

Un evento consiste di tre entità:

tag
time
record

- The tag is a string separated by ‘.’s (e.g. myapp.access), and is used as the directions for Fluentd’s internal routing engine. → Where an event comes from
- The time field is specified by input plugins, and it must be in the Unix time format. → When an event happens.
- The record is a JSON object. → Actual log content.





https://github.com/fluent/fluentd-kubernetes-daemonset/blob/master/README.md

Run as root
This is for v0.12 images.

In Kubernetes and default setting, fluentd needs root permission to read logs in /var/log and write pos_file to /var/log.
 To avoid permission error, you need to set FLUENT_UID environment variable to 0 in your Kubernetes configuration.





Use your configuration
These images have default configuration and support some environment variables for parameters but it sometimes doesn't fit your case. 
If you want to use your configuration, use ConfigMap feature.



Disable systemd input
If you don't setup systemd in the container, fluentd shows following messages by default configuration.

[warn]: #0 [in_systemd_bootkube] Systemd::JournalError: No such file or directory retrying in 1s
[warn]: #0 [in_systemd_kubelet] Systemd::JournalError: No such file or directory retrying in 1s
[warn]: #0 [in_systemd_docker] Systemd::JournalError: No such file or directory retrying in 1s
You can suppress these messages by setting disable to FLUENTD_SYSTEMD_CONF environment variable in your kubernetes configuration.

- name: FLUENTD_SYSTEMD_CONF
            value: "disable"
          - name: FLUENTD_CONF
            value: "custom-config/fluentd.conf"

================================================

Elasticsearch, Fluentd, and Kibana

Example
-------



Fatta partire docker

docker-compose -f docker-compose.yml -f httpd/httpd.yml up

e aggiunta alla configurazione di fluentd

<source>
  @type http
  port 9880
  bind 0.0.0.0
</source>


<filter inviodati.**>
  @type record_transformer
  <record>
    host_param "#{Socket.gethostname}"
  </record>
</filter>


<filter matchone.**>
  @type record_transformer
  <record>
    host_param "#{Socket.gethostname}"
  </record>
</filter>

<filter matchtwo.**>
  @type parser
  format json # apache2, nginx, etc...
  key_name log
  reserve_data true
</filter>

Fatta partire applicazione Spring Boot



docker-compose -f docker-compose.yml -f httpd/httpd.yml down


curl -X GET http://localhost:9880/b2b/fluent



docker logs esempiocdockerfluentd_fluentd_1 | tail -n 1



@id
The @id parameter is used to add the unique name of plugin configuration,
 which is used for paths of buffer/storage, logging and other purposes.


@log_level
This parameter is to specify plugin-specific logging level. 
The default log level is info. Global log level can be specified by log_level in <system>, or -v/-q command line options. 
The @log_level parameter overwrites logging level only for specified plugin instance.

<system>
  log_level info
</system>

<source>
  # ...
  @log_level debug  # show debug log only for this plugin
</source>



https://www.digitalocean.com/community/tutorials/how-to-use-journalctl-to-view-and-manipulate-systemd-logs
Introduction

Systemd avvelendosi centralizza i log del sytema globale

Il sistema che raccoglie e gestisce questi registri è noto come journal


Some of the most compelling advantages of systemd are those involved with process and system logging. When using other tools, logs are usually dispersed throughout the system, handled by different daemons and processes, and can be fairly difficult to interpret when they span multiple applications. Systemd attempts to address these issues by providing a centralized management solution for logging all kernel and userland processes. The system that collects and manages these logs is known as the journal.

The journal is implemented with the journald daemon, which handles all of the messages produced by the kernel, initrd, services, etc. In this guide, we will discuss how to use the journalctl utility, which can be used to access and manipulate the data held within the journal.