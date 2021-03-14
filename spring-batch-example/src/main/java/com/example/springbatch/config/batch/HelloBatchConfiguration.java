package com.example.springbatch.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HelloBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // Job을 만드는 팩토리
    private final StepBuilderFactory stepBuilderFactory; // Step을 만드는 팩토리

    public HelloBatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job helloJob() {
        return jobBuilderFactory.get("helloJob") // 잡 이름(key)
                .incrementer(new RunIdIncrementer()) // 파라미터 id 자동생성
                .start(this.helloStep())
                .build();
    }

    @Bean
    public Step helloStep() {
        return stepBuilderFactory.get("helloStep")
                .tasklet((contribution, chunkContext) -> { // task
                    log.info("hello spring batch");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
