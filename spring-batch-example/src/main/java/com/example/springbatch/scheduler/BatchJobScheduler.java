package com.example.springbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;

    public BatchJobScheduler(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    @Scheduled
    public void runJon() {

    }
}
