[![Actions Status](https://github.com/gridsuite/filters-server/workflows/CI/badge.svg)](https://github.com/gridsuite/filters-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Afilters-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Afilters-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)
# filter-server

### Run integration tests

 To automatically generate the sql schema file you can use the following command: 

```bash
mvn package -DskipTests && rm -f src/main/resources/filters.sql && java -jar target/gridsuite-filter-server-1.0.0-SNAPSHOT-exec.jar --spring.jpa.properties.javax.persistence.schema-generation.scripts.action=creat
```

