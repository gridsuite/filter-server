package org.gridsuite.filter.server.repositories;

import org.gridsuite.filter.server.entities.LineFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LineFilterRepository extends JpaRepository<LineFilterEntity, String> {
}
