package com.batch.api.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "BATCH_JOB_INSTANCE")
public class JobInstance extends PanacheEntityBase {

    @Id
    @Column(name = "JOB_INSTANCE_ID")
    public Long id;

    @Column(name = "JOB_NAME", nullable = false, length = 100)
    public String jobName;

    @Column(name = "JOB_KEY", nullable = false, length = 32)
    public String jobKey;

    @Column(name = "VERSION")
    public Integer version;
}
