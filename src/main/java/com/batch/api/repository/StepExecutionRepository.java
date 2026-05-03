package com.batch.api.repository;

import com.batch.api.model.StepExecution;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class StepExecutionRepository implements PanacheRepositoryBase<StepExecution, Long> {

    public List<StepExecution> findByJobExecutionId(Long jobExecutionId) {
        return list("jobExecution.id = ?1 ORDER BY id ASC", jobExecutionId);
    }
}
