package com.batch.api.service;

import com.batch.api.model.*;
import com.batch.api.model.BatchDTOs.*;
import com.batch.api.repository.JobExecutionRepository;
import com.batch.api.repository.StepExecutionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@ApplicationScoped
public class BatchService {

    private static final Logger LOG = Logger.getLogger(BatchService.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Inject
    JobExecutionRepository jobExecutionRepository;

    @Inject
    StepExecutionRepository stepExecutionRepository;

    // ------------------------------------------------------------------
    // GET /batch/executions  (paginado, com filtros)
    // ------------------------------------------------------------------
    public PagedResponse<JobExecutionDTO> getJobExecutions(
            String jobName,
            String status,
            String startDateFrom,
            String startDateTo,
            int page,
            int size) {

        JobStatus jobStatus = parseStatus(status);
        LocalDateTime from = parseDateTime(startDateFrom);
        LocalDateTime to   = parseDateTime(startDateTo);

        List<JobExecution> executions = jobExecutionRepository
                .findFiltered(jobName, jobStatus, from, to, page, size);

        long total = jobExecutionRepository
                .countFiltered(jobName, jobStatus, from, to);

        int totalPages = (int) Math.ceil((double) total / size);

        List<JobExecutionDTO> dtos = executions.stream()
                .map(JobExecutionDTO::from)
                .toList();

        return new PagedResponse<>(dtos, total, totalPages, size, page);
    }

    // ------------------------------------------------------------------
    // GET /batch/executions/{id}
    // ------------------------------------------------------------------
    public JobExecutionDTO getJobExecution(Long id) {
        return jobExecutionRepository
                .findByIdWithDetails(id)
                .map(JobExecutionDTO::from)
                .orElseThrow(() -> new NotFoundException(
                        "JobExecution não encontrado: id=" + id));
    }

    // ------------------------------------------------------------------
    // GET /batch/executions/{id}/steps
    // ------------------------------------------------------------------
    public List<StepExecutionDTO> getStepExecutions(Long jobExecutionId) {
        // Garante que a execução existe
        if (!jobExecutionRepository.findByIdOptional(jobExecutionId).isPresent()) {
            throw new NotFoundException(
                    "JobExecution não encontrado: id=" + jobExecutionId);
        }
        return stepExecutionRepository
                .findByJobExecutionId(jobExecutionId)
                .stream()
                .map(StepExecutionDTO::from)
                .toList();
    }

    // ------------------------------------------------------------------
    // GET /batch/jobs  (nomes distintos)
    // ------------------------------------------------------------------
    public List<String> getJobNames() {
        return jobExecutionRepository.findDistinctJobNames();
    }

    // ------------------------------------------------------------------
    // GET /batch/summary
    // ------------------------------------------------------------------
    public DashboardSummary getDashboardSummary() {
        long total     = jobExecutionRepository.count();
        long completed = jobExecutionRepository.count("status", JobStatus.COMPLETED);
        long failed    = jobExecutionRepository.count("status", JobStatus.FAILED);
        long running   = jobExecutionRepository.count("status", JobStatus.STARTED);
        long stopped   = jobExecutionRepository.count("status", JobStatus.STOPPED);
        double rate    = total == 0 ? 0 : Math.round((completed * 100.0) / total);

        return new DashboardSummary(total, completed, failed, running, stopped, rate);
    }

    // ------------------------------------------------------------------
    // POST /batch/executions/{id}/restart
    // ------------------------------------------------------------------
    @Transactional
    public void restartJobExecution(Long jobExecutionId) {
        JobExecution execution = jobExecutionRepository
                .findByIdOptional(jobExecutionId)
                .orElseThrow(() -> new NotFoundException(
                        "JobExecution não encontrado: id=" + jobExecutionId));

        if (execution.status != JobStatus.FAILED && execution.status != JobStatus.STOPPED) {
            throw new WebApplicationException(
                    "Apenas execuções com status FAILED ou STOPPED podem ser reiniciadas. " +
                    "Status atual: " + execution.status,
                    Response.Status.CONFLICT);
        }

        // Aqui você pode integrar com Spring Batch JobOperator, MQ, REST interno, etc.
        // Por ora marca como STARTING para sinalizar a intenção.
        execution.status = JobStatus.STARTING;
        execution.lastUpdated = LocalDateTime.now();
        jobExecutionRepository.persist(execution);

        LOG.infof("JobExecution %d marcada para restart (status -> STARTING)", jobExecutionId);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------
    private JobStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return JobStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException(
                    "Status inválido: " + status, Response.Status.BAD_REQUEST);
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            // Aceita ISO-8601 com ou sem segundos
            return LocalDateTime.parse(
                dateStr.length() == 10 ? dateStr + "T00:00:00" : dateStr,
                DATE_FORMATTER);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Formato de data inválido: " + dateStr +
                    ". Use yyyy-MM-dd ou yyyy-MM-dd'T'HH:mm:ss",
                    Response.Status.BAD_REQUEST);
        }
    }
}
