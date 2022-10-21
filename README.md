# es-recorder

### Server

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

### Elasticsearch

| Environment variable            | ARLAS Server configuration variable | Default        | Description                                                              |
|---------------------------------|-------------------------------------|----------------|--------------------------------------------------------------------------|
| ES_RECORDER_ELASTIC_NODES       | elastic.elastic-nodes               | localhost:9200 | coma separated list of elasticsearch nodes as host:port values           |
| ES_RECORDER_ELASTIC_ENABLE_SSL  | elastic.elastic-enable-ssl          | false          | use SSL to connect to elasticsearch                                      |
| ES_RECORDER_ELASTIC_CREDENTIALS | elastic.elastic-credentials         | user:password  | credentials to connect to elasticsearch                                  |
| ES_RECORDER_ELASTIC_SKIP_MASTER | elastic.elastic-skip-master         | true           | Skip dedicated master in Rest client                                     |

### CORS, HEADERS for API response

| Environment variable           | ARLAS Server configuration variable | Default                                                                                                                 | Description                                                      |
|--------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------|
| ARLAS_CORS_ENABLED             | arlas_cors.enabled                  | false                                                                                                                   | Whether to configure cors or not                                 |
| ARLAS_CORS_ALLOWED_ORIGINS     | arlas_cors.allowed_origins          | "*"                                                                                                                     | Comma-separated list of allowed origins                          |
| ARLAS_CORS_ALLOWED_HEADERS     | arlas_cors.allowed_headers          | "arlas-user,arlas-groups,arlas-organization,X-Requested-With,Content-Type,Accept,Origin,Authorization,X-Forwarded-User" | Comma-separated list of allowed headers                          |
| ARLAS_CORS_ALLOWED_METHODS     | arlas_cors.allowed_methods          | "OPTIONS,GET,PUT,POST,DELETE,HEAD"                                                                                      | Comma-separated list of allowed methods                          |
| ARLAS_CORS_ALLOWED_CREDENTIALS | arlas_cors.allowed_credentials      | true                                                                                                                    | Whether to allow credentials or not                              |
| ARLAS_CORS_EXPOSED_HEADERS     | arlas_cors.exposed_headers          | "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,Location"                                     | Comma-separated list of exposed headers, readable on client side |

### Logging

| Environment variable                          | ARLAS Server configuration variable                      | Default            |
|-----------------------------------------------|----------------------------------------------------------|--------------------|
| ES_RECORDER_LOGGING_LEVEL                     | logging.level                                            | INFO               |
| ES_RECORDER_LOGGING_CONSOLE_LEVEL             | logging.appenders[type: console].threshold               | INFO               |
| ES_RECORDER_LOGGING_FILE                      | logging.appenders[type: file].currentLogFilename         | es-recorder.log    |
| ES_RECORDER_LOGGING_FILE_LEVEL                | logging.appenders[type: file].threshold                  | INFO               |
| ES_RECORDER_LOGGING_FILE_ARCHIVE              | logging.appenders[type: file].archive                    | true               |
| ES_RECORDER_LOGGING_FILE_ARCHIVE_FILE_PATTERN | logging.appenders[type: file].archivedLogFilenamePattern | es-recorder-%d.log |
| ES_RECORDER_LOGGING_FILE_ARCHIVE_FILE_COUNT   | logging.appenders[type: file].archivedFileCount          | 5                  |
