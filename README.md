# Spring Batch Dashboard API — Quarkus 3.15.3.redhat-00002

API REST que expõe os endpoints consumidos pelo frontend Angular **Spring Batch Dashboard**.

## Endpoints

| Método | URL                                     | Descrição                              |
|--------|-----------------------------------------|----------------------------------------|
| GET    | `/batch/executions`                     | Lista paginada de JobExecutions        |
| GET    | `/batch/executions/{id}`                | JobExecution por ID (com steps)        |
| GET    | `/batch/executions/{id}/steps`          | Steps de uma execução                  |
| GET    | `/batch/jobs`                           | Nomes de jobs distintos                |
| GET    | `/batch/summary`                        | Totais para o dashboard                |
| POST   | `/batch/executions/{id}/restart`        | Reiniciar execução FAILED/STOPPED      |

### Query params para `GET /batch/executions`

| Param          | Tipo   | Exemplo                    |
|----------------|--------|----------------------------|
| `jobName`      | string | `processarPagamentos`      |
| `status`       | string | `FAILED`                   |
| `startDateFrom`| string | `2024-01-01T00:00:00`      |
| `startDateTo`  | string | `2024-12-31T23:59:59`      |
| `page`         | int    | `0`                        |
| `size`         | int    | `20`                       |

## Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL (ou ajuste o datasource em `application.properties`)

## Configuração do banco

A API lê as tabelas padrão do Spring Batch:

```
BATCH_JOB_INSTANCE
BATCH_JOB_EXECUTION
BATCH_JOB_EXECUTION_PARAMS
BATCH_STEP_EXECUTION
```

Essas tabelas são criadas automaticamente pelo Spring Batch na aplicação que processa os jobs.
Se quiser criar manualmente, use o schema oficial do Spring Batch para PostgreSQL.

## Rodando em desenvolvimento

```bash
# Configure variáveis de ambiente (ou edite application.properties)
export DB_URL=jdbc:postgresql://localhost:5432/batch_db
export DB_USER=batch_user
export DB_PASS=batch_pass

./mvnw quarkus:dev
```

Acesse:
- API: http://localhost:8080/batch
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI spec: http://localhost:8080/openapi

## CORS

Por padrão, as origens `http://localhost:4200` e `http://localhost:3000` estão liberadas.
Edite `application.properties`:

```properties
quarkus.http.cors.origins=http://localhost:4200,https://seu-dominio.com
```

## Build para produção

```bash
./mvnw package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### Native build (GraalVM)

```bash
./mvnw package -Pnative
./target/spring-batch-dashboard-api-1.0.0-SNAPSHOT-runner
```

## Estrutura do projeto

```
src/main/java/com/batch/api/
├── model/
│   ├── JobStatus.java          # Enum de status do job
│   ├── ExitStatus.java         # Enum de exit code
│   ├── JobInstance.java        # Entidade JPA (BATCH_JOB_INSTANCE)
│   ├── JobExecution.java       # Entidade JPA (BATCH_JOB_EXECUTION)
│   ├── StepExecution.java      # Entidade JPA (BATCH_STEP_EXECUTION)
│   └── BatchDTOs.java          # DTOs de response (records Java)
├── repository/
│   ├── JobExecutionRepository.java
│   └── StepExecutionRepository.java
├── service/
│   └── BatchService.java       # Lógica de negócio e filtragem
└── resource/
    ├── BatchResource.java       # Endpoints JAX-RS
    └── GlobalExceptionMapper.java
```
