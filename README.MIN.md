Deploy EFK

kubectl create -f pod-efk/


Deploy Registry

kubectl create -f pod-registry/kube-registry.yaml

kubectl -n kube-system get pods

kubectl port-forward -n kube-system kube-registry-v0-t5bz7 5000:5000

Creazione Image APP

eseguire 

./build-image-app.sh

invia la image nel registro locale creato sopra

Deploy image

kubectl create -f pod-app/

kubectl  get all

 kubectl  get all
NAME                                READY   STATUS    RESTARTS   AGE
pod/d-spamfluentd-d686c6fcf-mgf2b   1/1     Running   0          73s

NAME                    TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)          AGE
service/kubernetes      ClusterIP   10.96.0.1      <none>        443/TCP          20m
service/s-spamfluentd   NodePort    10.100.20.26   <none>        8080:32384/TCP   73s

NAME                            READY   UP-TO-DATE   AVAILABLE   AGE
deployment.apps/d-spamfluentd   1/1     1            1           73s

NAME                                      DESIRED   CURRENT   READY   AGE
replicaset.apps/d-spamfluentd-d686c6fcf   1         1         1       73s

kubectl logs d-spamfluentd-d686c6fcf-mgf2b

se tutto ok

minikube ip

192.168.39.183

inserire ip in /etc/hosts

192.168.39.183 spamfluentd


raggiungeremo la nostra applicazione tramite ip minikube e porta del servizio service/s-spamfluentd

curl -X GET http://spamfluentd:32384/fluentd/controller?param=spam_spam
