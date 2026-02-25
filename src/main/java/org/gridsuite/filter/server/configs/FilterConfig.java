package org.gridsuite.filter.server.configs;

import org.gridsuite.filter.api.FilterEvaluator;
import org.gridsuite.filter.api.FilterEvaluatorFactory;
import org.gridsuite.filter.server.RepositoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {
    @Bean
    public FilterEvaluator filterEvaluator(RepositoryService repositoriesService) {
        return FilterEvaluatorFactory.create(repositoriesService.getFilterLoader());
    }
}
