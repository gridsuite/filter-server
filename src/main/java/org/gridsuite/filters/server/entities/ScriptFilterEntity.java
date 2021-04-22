package org.gridsuite.filters.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.cassandra.core.mapping.Table;

@Getter
@NoArgsConstructor
@SuperBuilder
@Table("script_filters")
public class ScriptFilterEntity extends AbstractFilterEntity {
    String script;

}
