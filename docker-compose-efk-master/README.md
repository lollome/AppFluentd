Docker compose file for setting up a EFK service
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