# Filter Server

[![Actions Status](https://github.com/gridsuite/filter-server/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/filter-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Afilter-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Afilter-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

## Description

The **filter-server** is a microservice of the [GridSuite](https://github.com/gridsuite) platform dedicated to **storing and instantiating filters**.

Filters are sets of network equipment elements and are used in many places across the platform: to configure network modifications, to provide contingency lists to security analysis, to select equipment in sensitivity analysis, and to filter data in the UI. The service provides the following capabilities:

- **Store and manage filters** of two active types: identifier-list-based and expert-rule-based.
- **Evaluate filters** against a specific network from the network-store, returning the matched equipment as `FilterEquipments` objects.
- **Export filter results** ready to be consumed by other services (actions-server, sensitivity-analysis-server, network-modification-server, etc.).
- **Manage filters** (create, read, update, duplicate, delete) individually or in batch.
- **Notify** other microservices via RabbitMQ when a filter is modified.

The evaluation logic is provided by the [gridsuite-filter](https://github.com/gridsuite/filter) library. The server is responsible for persistence, REST exposure, cycle detection in cross-filter references, and service orchestration.

---

## Technical Stack

- Spring Boot (Web, Data JPA, Actuator, Cloud Stream)
- PostgreSQL
- Liquibase
- RabbitMQ via Spring Cloud Stream
- API documentation: OpenAPI / Swagger (`springdoc`)
- Micrometer / Prometheus
- [gridsuite-filter](https://github.com/gridsuite/filter) (core DTO and evaluation library)
- [powsybl-network-store-client](https://github.com/powsybl/powsybl-network-store)

---

## Filter Types

### Identifier List (`IDENTIFIER_LIST`)

An explicit list of **equipment IDs** with an optional `distributionKey` per entry (used for example as weighting for sensitivity analysis). Evaluation looks up each ID directly in the network and reports which IDs were not found.

### Expert (`EXPERT`)

A **rule tree** composed of logical combinators (`AND`, `OR`) and leaf rules comparing network equipment fields against values using a rich set of operators (`EQUALS`, `IN`, `BETWEEN`, `GREATER_THAN`, `IS_PART_OF`, ...).

Expert filters support all network equipment types and can reference other filter UUIDs via `IS_PART_OF` / `IS_NOT_PART_OF` operators. The server enforces **cycle detection** on every update to prevent circular references.

---

## Development Scripts

Build Docker image:

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

Please read [liquibase usage](https://github.com/powsybl/powsybl-parent/#liquibase-usage) for instructions to automatically generate changesets. After you generated a changeset do not forget to add it to git and in `src/main/resources/db/changelog/db.changelog-master.yml`.

---

## Interactions with Other Microservices

```
┌──────────────────────┐
│    filter-server     │──► network-store-server  (load network for filter evaluation)
└──────────────────────┘
         ▼
      RabbitMQ (element.update — emitted on single filter modification)
```

---

