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