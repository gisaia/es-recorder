# 1 Collecte des statistiques (es-recorder) : utilisation

## 1.1 Résumé

L'outil de collecte des statistiques des téléchargement est un microservice développé en java proposant une API REST qui alimente un index de données ElasticSearch. ElasticSearch est un moteur d'indexation de données déjà utilisé pour la recherche des métadonnées des méta-catalogues Dinamis et Theia. L'idée générale de l'outil est qu'à chaque fois qu'un utilisateur lance un téléchargement via le navigateur, le service REST est appelé et un nouveau document représentant cet événement de téléchargement est ajouté à l'index ElasticSearch.

Il nous est apparu primordial de développer un service le plus générique possible pouvant être réutilisé pour tout autre catalogue CNES ou IRD permettant le téléchargement d'archives et pour lesquels une perspective géo analytique est pertinente.

Ainsi l'outil de collecte utilise un modèle de données dynamique, il sait ajouter à l'index Elasticsearch tout objet JSON, comportant une partie fixe et une partie dynamique, si ce dernier est conforme au mapping de l'index dont le nom est défini dans sa configuration.

## 1.2 Présentation des APIs

L'outil s'interface exclusivement avec un cluster Elasticsearch.

Une entrée en base contient un id auto-généré (au format UUID), des données sur le client ayant lancé la requête de téléchargement (browser, hostname et ip) ainsi que le corps JSON initialement fourni à la requête de stockage.

Le service est sécurisé par un proxy Nginx. Le module "auth\_request" de Nginx est utilisé pour tout appel au service es\_recorder afin de vérifier la validité du token envoyé au service.

L'outil offre deux APIs.

### 1.2.1 API de management de la donnée

Cette API permet d'ajouter, de supprimer et de lire des entrées dans la base de données.

#### 1.2.1.1 Ajouter une donnée en base

- Endpoint

_POST /records/{index}_

- Description

Enregistre une nouvelle entrée dans l'index spécifié dans le path du endpoint.

- Paramètres

| **Type de paramètre** | **Nom**        | **Description**                              | **Type** | **Valeur par défaut** |
|-----------------------|----------------|----------------------------------------------|----------|-----------------------|
| Path                  | index_requis_  | Nom de l'index dans lequel écrire la donnée  | string   | aucune                |
| Body                  | record_requis_ | Valeur à persister sous forme de string JSON | string   | aucune                |

- Content type

Accepte et retourne "application/json;charset=utf-8"

- Response

Un JSON contenant l'id auto généré: {"id":"xxxx"}

#### 1.2.1.2 Lire une donnée

- Endpoint

_GET /records_/{index}/{id}

- Description

Retourne l'entrée correspondant à l'id.

- Paramètres

| **Type de paramètre** | **Nom**       | **Description**                             | **Type**      | **Valeur par défaut** |
|-----------------------|---------------|---------------------------------------------|---------------|-----------------------|
| Path                  | index_requis_ | Nom de l'index dans lequel écrire la donnée | string        | aucune                |
| Path                  | id_requis_    | Id de la donnée demandée                    | String (uuid) | aucune                |

- Content type

Accepte et retourne "application/json;charset=utf-8"

- Response

Un JSON l'objet demandé s'il existe.

#### 1.2.1.3 Effacer des données

- Endpoint

_DELETE /records_/{index}

- Description

Efface les données contenant le champ=valeur spécifié en paramètre (par exemple effacer les données dont le champ "foo" a pour valeur "bar"). Permet d'effacer par id ou tout autre champ.

- Paramètres

| **Type de paramètre** | **Nom**       | **Description**                             | **Type** | **Valeur par défaut** |
|-----------------------|---------------|---------------------------------------------|----------|-----------------------|
| Path                  | index_requis_ | Nom de l'index dans lequel écrire la donnée | string   | aucune                |
| Query                 | field_requis_ | Nom du champ                                | string   | aucune                |
| Query                 | value_requis_ | Valeur du champ                             | string   | aucune                |

- Content type

Accepte et retourne "application/json;charset=utf-8"

- Response

Un message confirmant la suppression.

### 1.2.2 API Swagger

L'API Swagger fournit une interface web pour tester l'API ainsi que des schémas standard de description d'API:

| **URL**                              | **Description**                               |
|--------------------------------------|-----------------------------------------------|
| http://.../es\_recorder/swagger      | L'application web swagger pour tester l'API   |
| http://.../es\_recorder/swagger.yaml | La définition swagger de l'API au format yaml |
| http://.../es\_recorder/swagger.json | La définition swagger de l'API au format JSON |

# 2 Schéma d'architecture

L'appel au service se fait directement par l'application front-end qui fournit le service dont on veut suivre les statistiques (le téléchargement dans le cas présent)

![](RackMultipart20221024-1-79158v_html_2d5e5bb2de116168.png)

_Schéma fonctionnel de l'outil de collecte_

![](RackMultipart20221024-1-79158v_html_e14ebc04d640dc76.png)

_Architecture globale_

Le schéma ci-dessus résume l'architecture globale :

- THEIA Frontend : actuel interface méta-catalogue Theia
- DINAMIS Frontend : actuel interface méta-catalogue Dinamis
- THEIA Backend : actuel serveur Arlas Theia
- DINAMIS Frontend : actuel serveur Arlas Dinamis
- Download Recorder : l'outil de collecte présenté dans ce document
- Elasticsearch : actuel cluster Elasticsearch
- Download Analytic Platform : stack Arlas d'exploration des statistiques
- XXX Frontend : toute application front-end qui voudrait collecter des données et les analyser avec une solution de géo-exploration de données

# 3 Configuration

## Server

| Environment variable            | ARLAS Server configuration variable                    | Default                      | Description                                                                         |
|---------------------------------|--------------------------------------------------------|------------------------------|-------------------------------------------------------------------------------------|
| ES_RECORDER_ACCESS_LOG_FILE     | server.requestLog.appenders.currentLogFilename         | es-recorder-access.log       |                                                                                     |
| ES_RECORDER_LOG_FILE_ARCHIVE    | server.requestLog.appenders.archivedLogFilenamePattern | es-recorder-access-%d.log.gz |                                                                                     |
| ES_RECORDER_APP_PATH            | server.applicationContextPath                          | /                            | Base URL path                                                                       |
| ES_RECORDER_PREFIX              | server.rootPath                                        | /es_recorder                 | Base sub-path for **general API**, gets appended to `server.applicationContextPath` |
| ES_RECORDER_ADMIN_PATH          | server.adminContextPath                                | /admin                       | Base sub-path for **admin API**, gets appended to `server.applicationContextPath`   |
| ES_RECORDER_PORT                | server.connector.port                                  | 9997                         |                                                                                     |
| ES_RECORDER_MAX_THREADS         | server.maxThreads                                      | 1024                         |                                                                                     |
| ES_RECORDER_MIN_THREADS         | server.minThreads                                      | 8                            |                                                                                     |
| ES_RECORDER_MAX_QUEUED_REQUESTS | server.maxQueuedRequests                               | 1024                         |                                                                                     |

## Elasticsearch

| Environment variable            | ARLAS Server configuration variable | Default        | Description                                                              |
|---------------------------------|-------------------------------------|----------------|--------------------------------------------------------------------------|
| ES_RECORDER_ELASTIC_NODES       | elastic.elastic-nodes               | localhost:9200 | coma separated list of elasticsearch nodes as host:port values           |
| ES_RECORDER_ELASTIC_ENABLE_SSL  | elastic.elastic-enable-ssl          | false          | use SSL to connect to elasticsearch                                      |
| ES_RECORDER_ELASTIC_CREDENTIALS | elastic.elastic-credentials         | user:password  | credentials to connect to elasticsearch                                  |
| ES_RECORDER_ELASTIC_SKIP_MASTER | elastic.elastic-skip-master         | true           | Skip dedicated master in Rest client                                     |

## CORS, HEADERS for API response

| Environment variable           | ARLAS Server configuration variable | Default                                                                                                                 | Description                                                      |
|--------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| ARLAS_CORS_ENABLED             | arlas_cors.enabled                  | false                                                                                                                   | Whether to configure cors or not                                 |
| ARLAS_CORS_ALLOWED_ORIGINS     | arlas_cors.allowed_origins          | "*"                                                                                                                     | Comma-separated list of allowed origins                          |
| ARLAS_CORS_ALLOWED_HEADERS     | arlas_cors.allowed_headers          | "arlas-user,arlas-groups,arlas-organization,X-Requested-With,Content-Type,Accept,Origin,Authorization,X-Forwarded-User" | Comma-separated list of allowed headers                          |
| ARLAS_CORS_ALLOWED_METHODS     | arlas_cors.allowed_methods          | "OPTIONS,GET,PUT,POST,DELETE,HEAD"                                                                                      | Comma-separated list of allowed methods                          |
| ARLAS_CORS_ALLOWED_CREDENTIALS | arlas_cors.allowed_credentials      | true                                                                                                                    | Whether to allow credentials or not                              |
| ARLAS_CORS_EXPOSED_HEADERS     | arlas_cors.exposed_headers          | "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location"                                     | Comma-separated list of exposed headers, readable on client side |

## Logging

| Environment variable                          | ARLAS Server configuration variable                      | Default            |
|-----------------------------------------------|----------------------------------------------------------|--------------------|
| ES_RECORDER_LOGGING_LEVEL                     | logging.level                                            | INFO               |
| ES_RECORDER_LOGGING_CONSOLE_LEVEL             | logging.appenders[type: console].threshold               | INFO               |
| ES_RECORDER_LOGGING_FILE                      | logging.appenders[type: file].currentLogFilename         | es-recorder.log    |
| ES_RECORDER_LOGGING_FILE_LEVEL                | logging.appenders[type: file].threshold                  | INFO               |
| ES_RECORDER_LOGGING_FILE_ARCHIVE              | logging.appenders[type: file].archive                    | true               |
| ES_RECORDER_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern | es-recorder-%d.log |
| ES_RECORDER_LOGGING_FILE_ARCHIVE_FILE_COUNT   | logging.appenders[type: file].archivedFileCount          | 5                  |

