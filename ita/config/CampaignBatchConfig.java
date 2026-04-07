package ita.config;

import ita.dto.EmailBatchDto;
import ita.entity.Contact;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;

import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;

import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.item.database.support.PostgresPagingQueryProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class CampaignBatchConfig extends DefaultBatchConfiguration {

//    @Bean
//    public JobOperator jobOperator(JobRepository jobRepository,
//                                   AsyncTaskExecutor campaignTaskExecutor) throws Exception {
//        TaskExecutorJobOperator operator = new TaskExecutorJobOperator();
//        operator.setJobRepository(jobRepository);
//        operator.setTaskExecutor(campaignTaskExecutor);
//        operator.afterPropertiesSet();
//        return operator;
//    }

    @Override
    protected TaskExecutor getTaskExecutor() {
        return campaignTaskExecutor();
    }

    @Bean
    public AsyncTaskExecutor campaignTaskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("Batch-Email-");
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(10);
        return executor;
    }

    //config Job
    @Bean
    public Job campaignBlastJob(JobRepository jobRepository, Step campaignBlastStep) {
        return new JobBuilder("campaignBlastJob", jobRepository)
                .start(campaignBlastStep)
                .build();
    }

    // config Step (Chunking & Multi-threading)
    @Bean
    public Step campaignBlastStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager,
                                  ItemReader<Contact> reader,
                                  ItemProcessor<Contact, EmailBatchDto> processor,
                                  ItemWriter<EmailBatchDto> writer,
                                  AsyncTaskExecutor campaignTaskExecutor) {
        return new StepBuilder("campaignBlastStep", jobRepository)
                .<Contact, EmailBatchDto>chunk(20) // Tarik x data per chunk
                .reader(reader)
                .transactionManager(transactionManager)
                .processor(processor)
                .writer(writer)
                .taskExecutor(campaignTaskExecutor)
                .build();
    }

    // Item Reader paging
    @Bean
    @StepScope
    public JdbcPagingItemReader<Contact> contactItemReader(
            DataSource dataSource,
            @Value("#{jobParameters['contactGroupId']}") String contactGroupId) throws Exception {

        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);

        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("SELECT c.id, c.email, c.name, c.is_unsubscribed");
        queryProvider.setFromClause("FROM contact c JOIN group_contact gc ON c.id = gc.contact_id");
        queryProvider.setWhereClause("WHERE gc.contact_group_id = :groupId AND (c.is_unsubscribed IS NULL OR c.is_unsubscribed = false)");
        queryProvider.setSortKeys(sortKeys);

        Map<String, Object> parameters = new HashMap<>();
        if (contactGroupId != null) {
            parameters.put("groupId", UUID.fromString(contactGroupId));
        }

        return new JdbcPagingItemReaderBuilder<Contact>()
                .name("contactItemReader")
                .dataSource(dataSource)
                .pageSize(1000)
                .queryProvider(queryProvider)
                .parameterValues(parameters)
                .rowMapper((rs, rowNum) -> {
                    Contact contact = new Contact();
                    contact.setId(UUID.fromString(rs.getString("id")));
                    contact.setEmail(rs.getString("email"));
                    contact.setName(rs.getString("name"));
                    Boolean isUnsubscribed = rs.getObject("is_unsubscribed", Boolean.class);
                    contact.setIsUnsubscribed(isUnsubscribed != null ? isUnsubscribed : false);

                    return contact;
                })
                .build();
    }
}