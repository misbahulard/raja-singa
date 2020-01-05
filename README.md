# Raja Singa [Lion King]
## Description
Raja Singa is app to sync with bastillion H2 database.

## Features
1. read csv (host list)
2. fetch host from phpipam
3. generate sql file
4. sync with H2 db directly

## Configuration
Raja Singa have config file called `app.properties` can be used in runtime compile or after packed to jar.

### Config Location
Place the config file in directory inside project for development, for jar place the config file in same directory with jar file. 

### Config structure
```
phpipam.url = <phpipam url, ex: http://example>
phpipam.app_id = <app id>
phpipam.username = <username>
phpipam.password = <password>
phpipam.dev_id = <section id of env, ex: 3>
phpipam.np_utility_id = <section id of env, ex: 3>
phpipam.qa_id = <section id of env, ex: 3>
phpipam.prod_id = <section id of env, ex: 3>
phpipam.dr_id = <section id of env, ex: 3>
domain.dev1 = <domain name of env, ex: example.lokal>
domain.dev2 = <domain name of env, ex: example.lokal>
domain.np_utility = <domain name of env, ex: example.lokal>
domain.qa1 = <domain name of env, ex: example.lokal>
domain.qa2 = <domain name of env, ex: example.lokal>
domain.prod = <domain name of env, ex: example.lokal>
domain.dr = <domain name of env, ex: example.lokal>
h2.url = <h2 db uri, ex: dbc:h2:/opt/server/bastillion;CIPHER=AES;
h2.user = <db user>
h2.pass = <user password>
```

## Build
Raja Singa is maven project, run this command to build jar
```
mvn clean compile assembly:single
```

## Run
This is the list of the flag:
```
-ipam           : fetch host from phpipam
-db             : sync with db instead of write to sql file
-user           : set user who will be inserted to hosts, ex: devops
-domain         : set domain name (needed when not fetch host from phpipam)
-port           : set ssh port number, default: 22
-authkeypath    : set ssh authorization key path, default: ~/.ssh/authorized_keys
-csvpath        : set csv file path to read
```
You just need to choose one, sync using phpipam or csv, and save the results to sql file or directly to db.

### Run Jar Example
Fetch from phpipam and write to sql file in qa environment
```
java -jar raja-singa-1.0-jar-with-dependencies.jar -ipam -user devops -env qa
```
Fetch from phpipam and sync with db in qa environment
```
java -jar raja-singa-1.0-jar-with-dependencies.jar -ipam -db -user devops -env qa
```
Read csv file and write to sql file in qa environment
```
java -jar raja-singa-1.0-jar-with-dependencies.jar -user devops -env qa -port 2209 -csvpath /opt/raja-singa/example.csv -domain example.lokal
```
Read csv file and sync with db in qa environment
```
java -jar raja-singa-1.0-jar-with-dependencies.jar -db -user devops -env qa -port 2209 -csvpath /opt/raja-singa/example.csv -domain example.lokal
```