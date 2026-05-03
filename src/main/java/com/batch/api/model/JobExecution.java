package com.batch.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "BATCH_JOB_EXECUTION")
public class JobExecution extends PanacheEntityBase {

    @Id
    @Column(name = "JOB_EXECUTION_ID")
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JOB_INSTANCE_ID", nullable = false)
    public JobInstance jobInstance;

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

    @Column(name = "CREATE_TIME", nullable = false)
    public LocalDateTime createTime;

    @Column(name = "LAST_UPDATED")
    public LocalDateTime lastUpdated;

    @Column(name = "VERSION")
    public Integer version;

    @OneToMany(mappedBy = "jobExecution", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    public List<StepExecution> stepExecutions;

    // Parâmetros do job são armazenados em tabela separada (BATCH_JOB_EXECUTION_PARAMS)
    // Mapeado como Map para compatibilidade com o frontend
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "BATCH_JOB_EXECUTION_PARAMS",
        joinColumns = @JoinColumn(name = "JOB_EXECUTION_ID")
    )
    @MapKeyColumn(name = "PARAMETER_NAME")
    @Column(name = "PARAMETER_VALUE")
    public Map<String, String> jobParameters;

    // Conveniência: retorna o jobName a partir da instância
    @Transient
    public String getJobName() {
        return jobInstance != null ? jobInstance.jobName : null;
    }

    @Transient
    public Long getJobInstanceId() {
        return jobInstance != null ? jobInstance.id : null;
    }
}
