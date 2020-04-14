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



By default, Kibana guesses that you’re working with log data being fed into Elasticsearch by Logstash.
[https://www.elastic.co/guide/en/kibana/current/index-patterns.html]

So, in the file fluentd.conf the value for index_name defaulted to ‘logstash’.

index_name “#{ENV[‘FLUENT_ELASTICSEARCH_LOGSTASH_INDEX_NAME’] || ‘fluentd’}”


logstash_format superseded the parameter index_name


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

Fatta partire applicazione Spring Boot



docker-compose -f docker-compose.yml -f httpd/httpd.yml down


curl -X GET http://localhost:9880/b2b/fluent



docker logs esempiocdockerfluentd_fluentd_1 | tail -n 1