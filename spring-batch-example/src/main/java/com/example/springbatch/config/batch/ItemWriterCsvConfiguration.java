package com.example.springbatch.config.batch;

import com.example.springbatch.chunk.CustomItemReader;
import com.example.springbatch.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ItemWriterCsvConfiguration {

    private final JobBuilderFactory jobBuilderFactory; // Job을 만드는 팩토리
    private final StepBuilderFactory stepBuilderFactory; // Step을 만드는 팩토리

    public ItemWriterCsvConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job itemWriterCsvJob() throws Exception {
        return jobBuilderFactory.get("itemWriterCsvJob") // 잡 이름(key)
                .incrementer(new RunIdIncrementer()) // 파라미터 id 자동생성
                .start(this.customItemReaderStep())
                .build();
    }

    public Step customItemReaderStep() throws Exception {
        return stepBuilderFactory.get("chunkBaseStep")
                .<Person, Person>chunk(10) // input type, output type을 generic type으로 지정
                .reader(new CustomItemReader<Person>(this.getItems()))
                .writer(this.csvItemWriter())
                .build();
    }

    private ItemWriter<Person> csvItemWriter() throws Exception {
        BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] {"id", "name", "age", "address"});

        DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Person> itemWriter = new FlatFileItemWriterBuilder<Person>()
                .name("csvItemWriter")
                .encoding("UTF-8")
                .resource(new FileSystemResource("output/test-output.csv"))
                .lineAggregator(lineAggregator)
                .headerCallback(writer -> writer.write("id,이름,나이,거주지"))
                .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();

        for (int i =0; i<100; i++) {
            items.add(new Person(i+1, "test name" + i, "test age", "test address"));
        }

        return items;
    }
}
