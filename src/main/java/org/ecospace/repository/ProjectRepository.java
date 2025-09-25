package org.ecospace.repository;

import org.ecospace.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface ProjectRepository extends JpaRepository<Project,UUID> {
}
