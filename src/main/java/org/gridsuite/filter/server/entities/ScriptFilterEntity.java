package org.gridsuite.filter.server.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "script_filter")
public class ScriptFilterEntity extends AbstractFilterEntity {
    @Column(name = "script")
    String script;

}
