package com.example.springbatch.scheduler;

import com.example.springbatch.config.batch.ChunkBatchConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    private final ChunkBatchConfiguration chunkBatchConfiguration;

    public BatchJobScheduler(JobLauncher jobLauncher, ChunkBatchConfiguration chunkBatchConfiguration) {
        this.jobLauncher = jobLauncher;
        this.chunkBatchConfiguration = chunkBatchConfiguration;
    }

    @Scheduled(fixedRate=10000) // 10초마다 1번씩 실행
    public void runJob() {

        Map<String, JobParameter> confMap = new HashMap<>();
        confMap.put("timestamp", new JobParameter(System.currentTimeMillis())); // 반복 수행을 위해 시간 값을 jobParameter에 추가
        JobParameters jobParameters = new JobParameters(confMap);

        try {

            jobLauncher.run(chunkBatchConfiguration.chunkJob(), jobParameters);

        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException
                | JobParametersInvalidException | org.springframework.batch.core.repository.JobRestartException e) {

            log.error(e.getMessage());
        }

    }
}
