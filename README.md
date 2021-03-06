
## Table of Contents

* [Description](#Description)
* [Dependances](#Dependances)
* [Installation](#installation)
* [What's included](#whats-included)
* [Comments](#Comments)
* [Convention de Codage](#Nomenclature)
* [Documentation](#documentation)
* [Contributing](#contributing)
* [Versioning](#versioning)
* [Creators](#creators)
* [Copyright and license](#license)
* [Support IDEL Development](#support-development)


## Description
Le module banq-authentication-service.
- DΓ©ploiement sous forme de micro-service

Il s'agit d'un programme java 11 dΓ©veloppΓ© avec le framework Spring Boot 2.4.3


## Dependances

* πͺ  [Java 17](https://www.java.com/)
* πͺ  [maven-3.6.2](https://maven.apache.org/)
* πͺ  [Spring Boot 2.6.6](https://spring.io/projects/spring-boot)
* πͺ  [Hibernate 5.3.3](https://hibernate.org/)
* πͺ  [Tomcat 9](http://tomcat.apache.org/)
* πͺ  [Swagger-UI 2.9.2](https://swagger.io/tools/swagger-ui/)
* πͺ  [Lombok 1.18](https://projectlombok.org/)
* πͺ  [H2 Database](https://www.h2database.com/html/main.html)
* πͺ  [Sonarlint]


## Installation

- Installer Java JDK17
- Installer Maven
- Installer sts (Spring Tool Suite) ou Intellij
- Installer les sources :

```bash
git clone https://djiomoufrancis@bitbucket.org/banq-dgtit/api-service-authentification.git
cd api-service-authentification
mvn -e clean package
```


## What's included

```
api-service-authentification/
βββ src/
β   βββ main/
β   β   βββ java/
β   β   β   βββ ca.qc.banq.gia.authentication/
β   β   β   βββ ca.qc.banq.gia.authentication.config/
β   β   β   βββ ca.qc.banq.gia.authentication.controller/
β   β   β   βββ ca.qc.banq.gia.authentication.entities/
β   β   β   βββ ca.qc.banq.gia.authentication.exceptions/
β   β   β   βββ ca.qc.banq.gia.authentication.filter/
β   β   β   βββ ca.qc.banq.gia.authentication.helpers/
β   β   β   βββ ca.qc.banq.gia.authentication.mapper/
β   β   β   βββ ca.qc.banq.gia.authentication.models/
β   β   β   βββ ca.qc.banq.gia.authentication.repositories/
β   β   β   βββ ca.qc.banq.gia.authentication.services/
β   β   βββ resources/
β   β   β   βββ config/
β   β   β   βββ docs/
β   β   β   βββ i18n/
β   β   β   βββ application.properties
β   βββ test/
β   β   βββ java/
β   β   β   βββ ca.qc.banq.gia.authentication/
β   β   β   βββ ca.qc.banq.gia.authentication.controller/
β   β   β   βββ ca.qc.banq.gia.authentication.mapper/
β   β   β   βββ ca.qc.banq.gia.authentication.repositories/
β   β   β   βββ ca.qc.banq.gia.authentication.services/
β   β   βββ resources/
β   β   β   βββ application-test.yml
βββ .gitignore
βββ README.md
βββ pom.xml
```


## Comments

- Package **ca.qc.banq.gia.authentication:** Package de base contenant la classe de demarrage de l'application
- Package **ca.qc.banq.gia.authentication.helpers:** Contient les objets partages par tous les composants de
  l'application
- Package **ca.qc.banq.gia.authentication.config:** Contient les differentes configurations
- Package **ca.qc.banq.gia.authentication.exception:** Contient les Exceptions
- Package **ca.qc.banq.gia.authentication.services:** Couche des traitements mΓ©tier
- Package **ca.qc.banq.gia.authentication.entities:** Couche des entitΓ©s du modΓ¨le de donnΓ©es
- Package **ca.qc.banq.gia.authentication.models:** Couche des modΓ¨les de donnΓ©es
- Package **ca.qc.banq.gia.authentication.controller:** Couche des webservices rest
- Package **ca.qc.banq.gia.authentication.mapper:** Couche de transformation DTO to Entity or Entity to DTO
- Package **ca.qc.banq.gia.authentication.repositories:** Couche pour operation sur les Entity
- ressource **config** Contient les differents profils de configuration
- ressource **docs** Contient les ressources representant la documentation des API et la documentation des sources du
  projet
- ressource **i18n** Contient les fichiers de messages utilises pour l'internationalisation
- ressource **application.properties** Fichier de configuration de base de l'application

**Differentes configurations de l'application**:  
		+ *AppConfig.java* : Configuration globale de l'application  
		+ *SwaggerConfig.java* : Configuration de la documentation Swagger  
		+ *WebMvcConfig.java* : Configuration MVC  
		+ *SecurityConfig.java* : Configuration de la securite d'acces a l'application  

```
| No | Package       | Description                                            |
|----|:-------------:|-------------------------------------------------------:|
|  1 | helpers       | utilitaires partagΓ©s                                   |
|  2 | config        | configurations du module                               |
|  3 | exception     | Les Exceptions                                         |
|  4 | models        | Les DTO                                                |
|  5 | service 		 | Les traitements mΓ©tier                                 |
|  6 | controller    | Webservices Rest                                       |
```

## Convention de Codage

- Les classes utilisent la convention de nommage standard dite de Β«**Camel Case**Β» (premiere letre de chaque mot en majuscule)
- Une classe qui implΓ©mente une interface se termine par Β«**Impl**Β»
- Les classes de type controlleurs contiennent le mot clΓ© Β«**Controller**Β»
- Les RΓ©fΓ©rentiels d'entitΓ©s sont des interfaces se terminant par le mot clΓ© Β«**Repository**Β»
- Les interfaces de services mΓ©tier se terminent par le mot clΓ© Β«**Service**Β»
- Les classes de configuration se terminent par le mot Β«**Config**Β»


## Documentation
#### API & Javadoc
The documentation for the banq-authentication-service is hosted at :
- Β» API Documentation : <http://localhost:9090/gia/v1/auth/apidoc/index.html>
![REST API Documentation](src/main/resources/docs/apidoc.png)  
- Β» Javadoc : <http://localhost:9090/gia/v1/auth/docs/javadoc/index.html>
![Javadoc](src/main/resources/docs/javadoc.png)  
- Β» Base de donnΓ©es H2 Embedded : <http://localhost:9090/gia/v1/auth/h2-console>
![Database](src/main/resources/docs/h2db.png)  
```
| No | Intitule                        | Valeur                  |
|----|:-------------------------------:|------------------------:|
|  1 | utilisateur de base de donnΓ©es  | sa                      |
|  2 | mot de passe de base de donnΓ©es | UEA1NXcwckQ=            |
|  3 | Datasource Url                  | jdbc:h2:mem:gia      	 |
```


## Tests
Deux application ont Γ©tΓ© configurΓ©es par dΓ©faut "**msal-b2c-web-sample**" pour une authentification Azure B2C et accessible via l'url
<http://localhost:9090/gia/v1/auth?appid=1>
ou encore l'application "**msal-web-sample**" pour tester une authentification Azure Active Directory et accessible via l'url
<http://localhost:9090/gia/v1/auth?appid=1>

	
## Le Build :

```bash
	cd api-service-authentification/
	mvn -e clean package
```
 
#### PrΓ©-requis
 
* Disposer d'un Serveur Linux au noyau RedHat
* Fichier banq-authentication-service.jar (gΓ©nΓ©rΓ© dans le rΓ©pertoire target/ aprΓ¨s le build du projet)
* Avoir installΓ© Java sur le serveur (ex: alternative par dΓ©faut sur /usr/bin/java)
 
#### Copier les ressources sur le serveur
- AccΓ©der au serveur	
- CrΓ©er le *WorkingDirectory* de l'application banq-authentication-service (ex: /opt/gia)

```bash
  sudo mkdir /opt/gia/
```
- Copier les ressources a dΓ©ployer (banq-authentication-service.jar et rΓ©pertoire config) dans le *WorkingDirectory*

```
  cp banq-authentication-service.jar /opt/gia/
```

#### CrΓ©er le service banq-authentication.service  
Ceci est nΓ©cessaire au premier dΓ©ploiement et permet de dΓ©marrer les service automatiquement par l'OS au dΓ©marrage du systΓ¨me

```
vim /etc/systemd/system/banq-authentication.service
```

- ajouter le contenu suivant au fichier

```
[Unit]
Description=BAnQ Authentification Service
Requires=network.service
After=network.service

[Service]
WorkingDirectory=/opt/gia
ExecStart=$JAVA_HOME/bin/java -jar /opt/gia/banq-authentication-service.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

- Sauvegarder et fermer le fichier

```
[Echap]
:wq!
[Enter]
```

- Activer le service

```
sudo systemctl enable banq-authentication.service
```
 
#### DΓ©marrer le service
```
sudo systemctl start banq-authentication.service
```
 
#### RedΓ©marrer le service
Cette action sera gΓ©nΓ©ralement indispensab;e en cas de mise a jour de l'application (ex: modification d'un fichier de configuration ou dΓ©ploiement d'une nouvelle version du fichier jar).
Executer la commande suivante :
```
sudo systemctl restart banq-authentication.service
```
 
#### ArrΓͺter le service
```
sudo systemctl stop banq-authentication.service
```
 
#### VΓ©rifier le statut du service
```
sudo systemctl status banq-authentication.service
```
 
#### Consulter en temps rΓ©el les logs du service
```
tail -lf /opt/gia/logs/banq-authentication.log
```



## Contributing

Please read through our [contributing guidelines] opening issues, coding standards, and notes on development.
http://jira.banq.qc.ca


## Versioning

For transparency into our release cycle and in striving to maintain backward compatibility, banq-authentication-service is maintained under http://nexus.banq.qc.ca/repository/maven-group/


## Creators

**BAnQ**

* <http://www.banq.qc.ca/accueil/>


## Copyright and license

copyright 2020 BAnQ.


## Support Development

banq-authentication-service is an BAnQ licensed open source project. However, the amount of effort needed to maintain and develop new features for the project is not sustainable without proper financial backing.
