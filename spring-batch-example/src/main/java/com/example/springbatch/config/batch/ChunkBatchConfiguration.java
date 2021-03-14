package com.example.springbatch.config.batch;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
public class ChunkBatchConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // Job을 만드는 팩토리
    private final StepBuilderFactory stepBuilderFactory; // Step을 만드는 팩토리

    public ChunkBatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job chunkJob() {
        return jobBuilderFactory.get("chunkJob") // 잡 이름(key)
                .incrementer(new RunIdIncrementer()) // 파라미터 id 자동생성
                .start(this.taskBaseStep())
                .next(this.chunkBaseStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step chunkBaseStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {

        int chunkSz = StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize): 10;

        return stepBuilderFactory.get("chunkBaseStep")
                .<String, String>chunk(chunkSz) // input type, output type을 generic type으로 지정
                .reader(this.itemReader())
                .processor(this.processor())
                .writer(this.writer())
                .build();
    }

    private ItemWriter<String> writer() {
        return items -> log.info("chunk item size: {}", items.size());
    }

    private ItemProcessor<? super String,? extends String> processor() { // 데이터 처리 및 writer로 넘길지말지 결정
        return item -> item + " spring";
    }

    private ItemReader<String> itemReader() { // 데이터를 읽어 드림
        return new ListItemReader<>(this.getItems());
    }

    @Bean
    public Step taskBaseStep() {
        return stepBuilderFactory.get("taskBaseStep")
                .tasklet(this.tasklet(null))
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet(@Value("#{jobParameters[chunkSize]}") String value) {
        return ((contribution, chunkContext) -> {

            List<String> items = this.getItems();

            StepExecution stepExecution = contribution.getStepExecution();
            int chunkSize = StringUtils.isNotBlank(value) ? Integer.parseInt(value): 10;

            int fromIndex = stepExecution.getReadCount();
            int toIndex = fromIndex + chunkSize;

            if(fromIndex >= items.size()) {
                return RepeatStatus.FINISHED;
            }

            List<String> subList = items.subList(fromIndex, toIndex);

            log.info("task item size: {}", subList.size());

            stepExecution.setReadCount(toIndex);

            return RepeatStatus.CONTINUABLE;
        });
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();

        for (int i =0; i<100; i++) {
            items.add("chunk");
        }

        return items;
    }
}
