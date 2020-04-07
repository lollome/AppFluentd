 
   
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

               
               
               
               
    
    