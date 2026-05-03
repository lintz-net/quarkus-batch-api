package com.batch.api.repository;

import com.batch.api.model.JobExecution;
import com.batch.api.model.JobStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class JobExecutionRepository implements PanacheRepositoryBase<JobExecution, Long> {

    /**
     * Busca paginada com filtros opcionais.
     */
    public List<JobExecution> findFiltered(
            String jobName,
            JobStatus status,
            LocalDateTime startDateFrom,
            LocalDateTime startDateTo,
            int page,
            int size) {

        var query = buildQuery(jobName, status, startDateFrom, startDateTo);

        return find(query.jpql(), Sort.by("createTime").descending(), query.params())
                .page(Page.of(page, size))
                .list();
    }

    /**
     * Conta total para paginação.
     */
    public long countFiltered(
            String jobName,
            JobStatus status,
            LocalDateTime startDateFrom,
            LocalDateTime startDateTo) {

        var query = buildQuery(jobName, status, startDateFrom, startDateTo);
        return count(query.jpql(), query.params());
    }

    /**
     * Busca execução pelo ID, carregando os steps e parâmetros.
     */
    public Optional<JobExecution> findByIdWithDetails(Long id) {
        return find(
            "SELECT DISTINCT e FROM JobExecution e " +
            "LEFT JOIN FETCH e.jobInstance " +
            "LEFT JOIN FETCH e.stepExecutions " +
            "LEFT JOIN FETCH e.jobParameters " +
            "WHERE e.id = ?1", id
        ).firstResultOptional();
    }

    /**
     * Retorna nomes de jobs distintos ordenados.
     */
    public List<String> findDistinctJobNames() {
        return getEntityManager()
            .createQuery(
                "SELECT DISTINCT i.jobName FROM JobInstance i ORDER BY i.jobName",
                String.class)
            .getResultList();
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private record QuerySpec(String jpql, Map<String, Object> params) {}

    private QuerySpec buildQuery(
            String jobName,
            JobStatus status,
            LocalDateTime startDateFrom,
            LocalDateTime startDateTo) {

        var conditions = new ArrayList<String>();
        var params = new HashMap<String, Object>();

        if (jobName != null && !jobName.isBlank()) {
            conditions.add("jobInstance.jobName LIKE :jobName");
            params.put("jobName", "%" + jobName.trim() + "%");
        }
        if (status != null) {
            conditions.add("status = :status");
            params.put("status", status);
        }
        if (startDateFrom != null) {
            conditions.add("startTime >= :startDateFrom");
            params.put("startDateFrom", startDateFrom);
        }
        if (startDateTo != null) {
            conditions.add("startTime <= :startDateTo");
            params.put("startDateTo", startDateTo);
        }

        String jpql = conditions.isEmpty()
            ? "jobInstance IS NOT NULL"
            : String.join(" AND ", conditions);

        return new QuerySpec(jpql, params);
    }
}
