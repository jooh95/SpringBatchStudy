package com.example.springbatch.config.batch;

import com.example.springbatch.chunk.CustomItemReader;
import com.example.springbatch.model.Person;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // Job을 만드는 팩토리
    private final StepBuilderFactory stepBuilderFactory; // Step을 만드는 팩토리

    public ItemReaderConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job itemReaderJob() {
        return jobBuilderFactory.get("itemReaderJob") // 잡 이름(key)
                .incrementer(new RunIdIncrementer()) // 파라미터 id 자동생성
                .start(this.customItemReaderStep(null))
                .build();
    }

    @Bean
    @JobScope
    public Step customItemReaderStep(@Value("#{jobParameters[chunkSize]}") String chunkSize) {

        int chunkSz = StringUtils.isNotEmpty(chunkSize) ? Integer.parseInt(chunkSize): 10;

        return stepBuilderFactory.get("chunkBaseStep")
                .<Person, Person>chunk(chunkSz) // input type, output type을 generic type으로 지정
                .reader(new CustomItemReader<Person>(this.getItems()))
                .writer(this.itemWriter())
                .build();
    }

    private ItemWriter<Person> itemWriter() {
        return items -> {
            log.info(items.stream()
                    .map(Person::getName)
                    .collect(Collectors.joining(",")));
        };
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();

        for (int i =0; i<100; i++) {
            items.add(new Person(i+1, "test name" + i, "test age", "test address"));
        }

        return items;
    }
}
