 
   ## Progetto EFK (Elasticsearch-Fluentd-Kibana )
   
   Lo scopo del progetto è quello di scrivere i log sullo stack EFK (Elasticsearch Fluentd Kibana)
   deployando il progetto in un cluster kubernates creato tramite minikube
   
   nel branch master il progetto di un sistema efk su docker e applicazione locale per provare i test prima di portarli in kubernates
   
   ### Passi per deploy dell'applicazione AppFluentd in un cluster Kubernates creato tramite Minikube

      
   Attraverso minikube possiamo installare un cluster e con kubelect di Kubernates (k8s) possiamo deployare
   tutti gli oggetti necessari per il sistema che vogliamo creare
   
   A) Installazione del POD EFK (Elastichsearch Fluentd Kibana)
   B) Installazione del POD registry dove depositiamo l'image della app
   C) Installazione del POD Appliazione AppFluentd
   
   la creazione degli oggetti viene effettutata

    $ kubectl create - f pathfileyaml o dir
    
   ##### A - Installazione del POD EFK (Elastichsearch Fluentd Kibana)
       
    $ kubectl create -f pod-efk/ 
        
   con il comando di segutio esploriamo tutti gli oggetti installato 
        
    $ kubectl -n kube-logging get all
    
    situazione iniziale
    NAME                         READY   STATUS              RESTARTS   AGE
    pod/es-cluster-0             0/1     Init:0/3            0          20s
    pod/fluentd-txlvt            0/1     Init:0/1            0          20s
    pod/kibana-74db58d68-lnt9n   0/1     ContainerCreating   0          20s
    
    
   i pod sono in creazione
    
   alla fine avremo
    
    
    NAME                         READY   STATUS    RESTARTS   AGE
    pod/es-cluster-0             1/1     Running   0          10h
    pod/fluentd-4czgx            1/1     Running   0          10h
    pod/kibana-74db58d68-hdwkt   1/1     Running   0          10h
    
    NAME                    TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)             AGE
    service/elasticsearch   ClusterIP   None             <none>        9200/TCP,9300/TCP   10h
    service/fluentd         ClusterIP   10.103.129.130   <none>        24224/TCP           10h
    service/kibana          NodePort    10.111.77.6      <none>        5601:30574/TCP      10h
    
    NAME                     DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
    daemonset.apps/fluentd   1         1         1       1            1           <none>          10h
    
    NAME                     READY   UP-TO-DATE   AVAILABLE   AGE
    deployment.apps/kibana   1/1     1            1           10h
    
    NAME                               DESIRED   CURRENT   READY   AGE
    replicaset.apps/kibana-74db58d68   1         1         1       10h
    
    NAME                          READY   AGE
    statefulset.apps/es-cluster   1/1     10h

        
   I primi tre oggetto sono i tre POD del EFK e subito sotto i rispettivi servizi con cui ci possiamo interfacciare dall'esterno
        
   Quello che ci interessa è che il servizio fluentd risponde, internamente al cluster, con i seguenti parametri
   
    host =  fluentd.kube-logging.svc.cluster.local
    port = 24224
       
   per cui nell'applicazione AppFluentd, class it.logging.CustomFluentLogger ho configuato come segue
       
       
    public static FluentLogger getLogger(String tagPrefix)
    {
       return FluentLogger.getLogger(tagPrefix,"fluentd.kube-logging.svc.cluster.local",24224);
    }
    
   
   possiamo avviare kibana con il seguente comando 
    
    
    $ minikube -n kube-logging service kibana 
   
   (diamogli un bel po di tempo!!!!)
   
    ci verrà presentata la dashboard di kibana dove è possibile creare l'indice e osservare i vari log (dopo che eseguiamo i punti successivi)    
      
   ##### B - Installazione del POD registry dove depositiamo l'image della app
   
    
   Dobbiamo memorizzare l'applicazizone in un registro perchè sarà il container del POD dell'applicazione
               
   
     a. Creare il registry su minikube (dal file yml)
             
             $ kubectl create -f pod-registry/kube-registry.yaml
             
           possiamo visualizzare i pods e vedere specificatamente il kube-registry-v0-xxxx creato; 
             
             $ kubectl get po -n kube-system | grep kube-registry-v0 | \awk '{print $1;}'
             
           in caso non sia in stato di Running (tipo ImagePullBackOff) è possibile vedere possibili errori con il comando 
                             
             $ kubectl -n kube-system describe pod kube-registry-v0-xxxxx
             
             se andato tutto bene facciamo il forward -> punto b
     
     b. Forward della porta 5000 da localhost a minikube
        
         esponiamo il servizio all'esterno
 
             $ kubectl port-forward -n kube-system kube-registry-v0-b2n5w 5000:5000
 
           N.B: il nome del pod varia ogni volta.
 
   
   ##### C - Installazione del POD Appliazione AppFluentd
   
   Creaiamo la nostra image AppFluentd che servirà per creare il container del POD dell'applicazione 
   
   
   1.  Creazione del jar
          
    $ ./gradlew build
       
   2. Preparazione delle dipendenze per l'applicazione per quando girerà sulla docker
       
    $ mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)
      
      
   3. Build della Image
      
    $ docker build  (--build-arg DEPENDENCY=build/dependency) -t HOST_REGISTRY/appfluentd/spam-fluentd:latest --rm=true .
  
    -t => (--tag) seguito da name:versione prepara la image che andremo a memorizzare dentro un registry locale 
  
   infatti se eseguiamo il comando 
           
   $ docker images 
       
  vedremo come è memorizzata in docker
   
    REPOSITORY                                      TAG                 IMAGE ID            CREATED             SIZE
    appfluentd/spam-fluentd:1.0                         latest              fefedca30972        15 seconds ago      137MB
          
          
  Possiamo anche  eseguire localmente la nostra appplicazione
         
    $ docker run -p 8080:8080 -t appfluentd/spam-fluentd:1.0
           
  vedremo la nostra app in background; per vedere i log
           
    $ docker ps -a

    CONTAINER ID        IMAGE                        COMMAND                  CREATED             STATUS                       PORTS               NAMES
    9b36109e3110        appfluentd/spam-fluentd:1.0  "java -cp app:app/li…"   2 minutes ago       Exited (1) 2 minutes ago                         agitated_sammet

   
    $ docker logs 9b36109e3110 | tail -n 10
        
    (docker logs container_id | tail -n 10)
   
    
      
  Quindi creiamo l'image dell'applicazione per poterla inviare al registry locale
   
   Build/tag e push 
     
     $ docker build -t localhost:5000/appfluentd/spam-fluentd:1.0 .
     
     $ docker push localhost:5000/appfluentd/spam-fluentd:1.0
     
   Ora è disponibile su un registry ed è possibile usarla come container in un deployement
     
        
   Finalmente possiamo creare il POD dell'applicazione
   
   eseguimo il comando 
        
     $ kubectl create -f pod-app/
        
   in ordine creerà tutti gli oggetti descritti nei seguenti file yaml
        
    10-secret.yaml
    11-service.yaml
    12-deployment.yaml
    20-ingress.yaml
            
       
   Il deployment.yaml mi crea il container con l' image dell'applicazione creata e che caricata nel registry locale 
   ()e un volume dove inserisco una chiave privata che ora non mi serve a nulla!!! TODO)
                   
   Il service.yaml è un modo astratto che mi dice come esporre un'applicazione in esecuzione su un set di pod come servizio di rete.
   
   Per esporre il servizio all'eserno ho bisogno dell'ingress.yaml
                   
   per esporlo all'esterno ho necissità di altre configurazioni:
           
   - To enable the NGINX Ingress controller, run the following command:
           
        $ minikube addons enable ingress    
                           
        $ kubectl get ingress
            NAME              HOSTS         ADDRESS   PORTS   AGE
            webspam-ingress   spamfluentd             80      6h30m
      
      
      
        
   Quindi possiamo testare se tutto è andato a buon fine
   con i seguenti comandi
            
    $ kubectl get all
        
    NAME                                READY   STATUS    RESTARTS   AGE
    pod/d-spamfluentd-646d69dc8-trljx   1/1     Running   0          6h12m
    
    NAME                    TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)          AGE
    service/kubernetes      ClusterIP   10.96.0.1    <none>        443/TCP          10h
    service/s-spamfluentd   NodePort    10.98.83.2   <none>        8080:31470/TCP   6h12m
    
    NAME                            READY   UP-TO-DATE   AVAILABLE   AGE
    deployment.apps/d-spamfluentd   1/1     1            1           6h12m
    
    NAME                                      DESIRED   CURRENT   READY   AGE
    replicaset.apps/d-spamfluentd-646d69dc8   1         1         1       6h12m

        
        
        
        
    $ kubectl logs d-spamfluentd-646d69dc8-trljx
        
   stampa dei log dell'applicazione
        
            
       $ kubectl logs d-spamfluentd-646d69dc8-trljx
            
          Name:         d-spamfluentd-646d69dc8-trljx
          Namespace:    default
          Priority:     0
          Node:         m01/192.168.39.139
          Start Time:   Sun, 12 Apr 2020 14:18:02 +0200
          Labels:       app=spamfluentd
                        pod-template-hash=646d69dc8
                        release=spamfluentd-1
          Annotations:  <none>
          Status:       Running
          IP:           172.17.0.9
          IPs:
            IP:           172.17.0.9
          Controlled By:  ReplicaSet/d-spamfluentd-646d69dc8
          Containers:
            spamfluentd:
              Container ID:   docker://666df4dc4253fa4e0b095257cb7188b6d9ab12a9288913ceffa35d57ca8355ef
              Image:          localhost:5000/appfluentd/spam-fluentd:1.0
              Image ID:       docker-pullable://localhost:5000/appfluentd/spam-fluentd@sha256:c8088189cb10b85c76195538e00bd8587fa19dd8cd84e0851a47a97a0bb5e34c
              Port:           8080/TCP
              Host Port:      0/TCP
              State:          Running
                Started:      Sun, 12 Apr 2020 14:18:10 +0200
              Ready:          True
              Restart Count:  0
              Requests:
                cpu:        100m
              Environment:  <none>
              Mounts:
                /etc/app.d/01-secret/ from secret-app (rw)
                /var/run/secrets/kubernetes.io/serviceaccount from default-token-zbhgg (ro)
          Conditions:
            Type              Status
            Initialized       True 
            Ready             True 
            ContainersReady   True 
            PodScheduled      True 
          Volumes:
            secret-app:
              Type:        Secret (a volume populated by a Secret)
              SecretName:  app-035002
              Optional:    false
            default-token-zbhgg:
              Type:        Secret (a volume populated by a Secret)
              SecretName:  default-token-zbhgg
              Optional:    false
          QoS Class:       Burstable
          Node-Selectors:  <none>
          Tolerations:     node.kubernetes.io/not-ready:NoExecute for 300s
                           node.kubernetes.io/unreachable:NoExecute for 300s
          Events:          <none>

            
          
   Con quest'ultimo comando possiamo capire se è stata scaricata l'image della nostra applicazione
          
   Ci romane da configurare l'host per poter accedere dall'esterno all'applicazione.
           
   Add the following line to the bottom of the /etc/hosts file.
         
     echo "$(minikube ip) spamfluentd" | sudo tee -a /etc/hosts
     
     oppure aggiungere a mano nel file /etc/hostes l'ip  $ minikube ip
     
     192.168.39.139 spamfluentd
     
     mnetre con il comando possiamo capire la porta
     
     
     $ kubectl get service s-spamfluentd 
         NAME            TYPE       CLUSTER-IP   EXTERNAL-IP   PORT(S)          AGE
         s-spamfluentd   NodePort   10.98.83.2   <none>        8080:31470/TCP   6h39m
         
      il servizio risponde alla porta 31470
         
      $ curl http://spamfluentd:31470/fluentd/matchone?param=ciao
      $ curl http://spamfluentd:31470/fluentd/matchtwo?param=ciao
      $ curl http://spamfluentd:31470/fluentd/docker?param=ciao
          
     
     
     
               
           
     Ora da kibana possiamo vedere i log che sono stampati dai 3 controller
           
   -------------------
   
     pjd.spamfluentd -> pjd.pjd.spamfluentd
     
     
     kubectl -n kube-logging  exec fluentd-lt26h  -it /bin/bash
     
     cd /var/log
     
     drwxr-xr-x  4 root root 4096 Apr 20 07:13 .
     drwxr-xr-x  1 root root 4096 Oct 11  2018 ..
     drwxr-xr-x  2 root root 4096 Apr 20 07:14 containers
     -rw-r--r--  1 root root   66 Apr 20 07:13 fluentd-cluster-autoscaler.log.pos
     -rw-r--r--  1 root root 2502 Apr 20 07:52 fluentd-containers.log.pos
     -rw-r--r--  1 root root   54 Apr 20 07:13 fluentd-docker.log.pos
     -rw-r--r--  1 root root   52 Apr 20 07:13 fluentd-etcd.log.pos
     -rw-r--r--  1 root root   52 Apr 20 07:13 fluentd-glbc.log.pos
     -rw-r--r--  1 root root   62 Apr 20 07:13 fluentd-kube-apiserver.log.pos
     -rw-r--r--  1 root root   71 Apr 20 07:13 fluentd-kube-controller-manager.log.pos
     -rw-r--r--  1 root root   58 Apr 20 07:13 fluentd-kube-proxy.log.pos
     -rw-r--r--  1 root root   62 Apr 20 07:13 fluentd-kube-scheduler.log.pos
     -rw-r--r--  1 root root   55 Apr 20 07:13 fluentd-kubelet.log.pos
     -rw-r--r--  1 root root   59 Apr 20 07:13 fluentd-rescheduler.log.pos
     -rw-r--r--  1 root root   55 Apr 20 07:13 fluentd-salt.pos
     -rw-r--r--  1 root root   61 Apr 20 07:13 fluentd-startupscript.log.pos
     -rw-r--r--  1 root root   79 Apr 20 07:13 kube-apiserver-audit.log.pos
     drwxr-xr-x 13 root root 4096 Apr 20 07:11 pods
     
     
     coredns-6955765f44-7xgj7_kube-system_coredns-c565ba3df8dc13d60f2da00f4bae83413b5a0fbcae5c7b9f112fbf5eac72898c.log
     coredns-6955765f44-xfdms_kube-system_coredns-647f57f4e91961fe06fbf60795dcd7d0f2dff771beb96b8776632901fa790c5b.log
     es-cluster-0_kube-logging_elasticsearch-4a32280224563a4452c0eef3c79e0dd9ff0b5a0ead22e38ae91d7d09ebc8d4d3.log
     es-cluster-0_kube-logging_fix-permissions-3b77dd0c26339535ac35aca0a9cf0c8b5aff84c41b3e1e17209742e7035ac110.log
     es-cluster-0_kube-logging_increase-fd-ulimit-ab9dd0f3eb996b27526c6f0a9c84918645e0fa49a6b5a285e5e4fcb5875eb4f4.log
     es-cluster-0_kube-logging_increase-vm-max-map-4b63aa7b068c0fa68f91247724aeca43f630d68588898c402ccb918905681628.log
     etcd-m01_kube-system_etcd-afe30d0115b1256c1eebbc32a56fab7e141145b3ace895e78d03207f11562d5f.log
     fluentd-nvvn5_kube-logging_config-fluentd-450d82c782819149d7a31553c422c79fe4dbe7b047cd5404145bc608beec6826.log
     fluentd-nvvn5_kube-logging_fluentd-69e03e56bfe40e7fb5ed164ce485afd2b49975bf15e55bd1d149690008f7f913.log
     kibana-74db58d68-qmvqr_kube-logging_kibana-7433fc151c22cec0ec988c4bb92bc222ce3b51d990a33b2ea28327ac9a47478c.log
     kube-apiserver-m01_kube-system_kube-apiserver-702f8c1df88b875ab11e3eb8f12cd18294731e5dc7dd0838e40e89df602f3ae4.log
     kube-controller-manager-m01_kube-system_kube-controller-manager-c56cc748effb6d24e0dafd919633beef425b63f97b816d5d31034d309329a397.log
     kube-proxy-72mkk_kube-system_kube-proxy-8dcba6d17c5970adb45e3a5f05e9b32dab82bb3c9865e20d2b2e800e8f14cc95.log
     kube-scheduler-m01_kube-system_kube-scheduler-5677ee79431ae12da17eac75482fff62bdecb227b8f8744ae0b9eb92163d9ea5.log
     storage-provisioner_kube-system_storage-provisioner-bd19fc1e39a1898deb6607d5b2b2faba1392b124b2f81b4c12e09cfcf3e15ec9.log
     
     
     kubectl -n kube-logging get pods -o json > containers.txt
     
     
     systemd input plugin to read logs from the systemd journal
     
     
     fluentd -c /fluentd/etc/${FLUENTD_CONF} -p /fluentd/plugins ${FLUENTD_OPT} -vv