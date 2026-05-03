package com.batch.api.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs que espelham exatamente os contratos esperados pelo frontend Angular.
 */
public final class BatchDTOs {

    private BatchDTOs() {}

    // ---------------------------------------------------------------
    // Response: JobExecution
    // ---------------------------------------------------------------
    public record JobExecutionDTO(
        Long id,
        Long jobInstanceId,
        String jobName,
        JobStatus status,
        ExitStatus exitCode,
        String exitMessage,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createTime,
        LocalDateTime lastUpdated,
        Map<String, String> jobParameters,
        List<StepExecutionDTO> stepExecutions
    ) {
        public static JobExecutionDTO from(JobExecution e) {
            List<StepExecutionDTO> steps = e.stepExecutions == null
                ? List.of()
                : e.stepExecutions.stream().map(StepExecutionDTO::from).toList();

            return new JobExecutionDTO(
                e.id,
                e.getJobInstanceId(),
                e.getJobName(),
                e.status,
                e.exitCode,
                e.exitMessage,
                e.startTime,
                e.endTime,
                e.createTime,
                e.lastUpdated,
                e.jobParameters != null ? e.jobParameters : Map.of(),
                steps
            );
        }
    }

    // ---------------------------------------------------------------
    // Response: StepExecution
    // ---------------------------------------------------------------
    public record StepExecutionDTO(
        Long id,
        String stepName,
        JobStatus status,
        ExitStatus exitCode,
        String exitMessage,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int readCount,
        int writeCount,
        int commitCount,
        int rollbackCount,
        int readSkipCount,
        int processSkipCount,
        int writeSkipCount,
        int filterCount
    ) {
        public static StepExecutionDTO from(StepExecution s) {
            return new StepExecutionDTO(
                s.id, s.stepName, s.status, s.exitCode, s.exitMessage,
                s.startTime, s.endTime,
                s.readCount, s.writeCount, s.commitCount, s.rollbackCount,
                s.readSkipCount, s.processSkipCount, s.writeSkipCount, s.filterCount
            );
        }
    }

    // ---------------------------------------------------------------
    // Response: Paginação
    // ---------------------------------------------------------------
    public record PagedResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int size,
        int number
    ) {}

    // ---------------------------------------------------------------
    // Response: Dashboard Summary
    // ---------------------------------------------------------------
    public record DashboardSummary(
        long totalExecutions,
        long completed,
        long failed,
        long running,
        long stopped,
        double successRate
    ) {}
}
