package com.batch.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BATCH_STEP_EXECUTION")
public class StepExecution extends PanacheEntityBase {

    @Id
    @Column(name = "STEP_EXECUTION_ID")
    public Long id;

    @Column(name = "STEP_NAME", nullable = false, length = 100)
    public String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 10)
    public JobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "EXIT_CODE", length = 20)
    public ExitStatus exitCode;

    @Column(name = "EXIT_MESSAGE", length = 2500)
    public String exitMessage;

    @Column(name = "START_TIME")
    public LocalDateTime startTime;

    @Column(name = "END_TIME")
    public LocalDateTime endTime;

    @Column(name = "READ_COUNT")
    public int readCount;

    @Column(name = "WRITE_COUNT")
    public int writeCount;

    @Column(name = "COMMIT_COUNT")
    public int commitCount;

    @Column(name = "ROLLBACK_COUNT")
    public int rollbackCount;

    @Column(name = "READ_SKIP_COUNT")
    public int readSkipCount;

    @Column(name = "PROCESS_SKIP_COUNT")
    public int processSkipCount;

    @Column(name = "WRITE_SKIP_COUNT")
    public int writeSkipCount;

    @Column(name = "FILTER_COUNT")
    public int filterCount;

    @Column(name = "VERSION")
    public Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_EXECUTION_ID")
    @JsonBackReference
    public JobExecution jobExecution;
}
