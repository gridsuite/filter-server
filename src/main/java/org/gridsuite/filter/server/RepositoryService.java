package org.gridsuite.filter.server;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.collections4.ListUtils;
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.server.dto.FilterMetadataDto;
import org.gridsuite.filter.server.repositories.expertfilter.ExpertFilterRepository;
import org.gridsuite.filter.server.repositories.identifierlistfilter.IdentifierListFilterRepository;
import org.gridsuite.filter.server.repositories.proxies.AbstractFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.expertfiler.ExpertFilterRepositoryProxy;
import org.gridsuite.filter.server.repositories.proxies.identifierlistfilter.IdentifierListFilterRepositoryProxy;
import org.gridsuite.filter.utils.FilterType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * More an utility service to call a methode on all {@link AbstractFilterRepositoryProxy repositories}
 * and merge the result.
 */
@Service
public class RepositoryService {
    private final IdentifierListFilterRepositoryProxy identifierListFilterProxy;
    private final ExpertFilterRepositoryProxy expertFilterProxy;
    @Getter private final FilterLoader filterLoader;

    public RepositoryService(final IdentifierListFilterRepository identifierListFilterRepository,
                             final ExpertFilterRepository expertFilterRepository) {
        this.identifierListFilterProxy = new IdentifierListFilterRepositoryProxy(identifierListFilterRepository);
        this.expertFilterProxy = new ExpertFilterRepositoryProxy(expertFilterRepository);
        this.filterLoader = new FilterLoaderImpl(Map.of(
            FilterType.IDENTIFIER_LIST.name(), this.identifierListFilterProxy,
            FilterType.EXPERT.name(), this.expertFilterProxy
        ));
    }

    public AbstractFilterRepositoryProxy<?, ?> getRepositoryFromType(@NonNull final FilterType type) {
        return switch (type) {
            case IDENTIFIER_LIST -> identifierListFilterProxy;
            case EXPERT -> expertFilterProxy;
        };
    }

    public AbstractFilterRepositoryProxy<?, ?> getRepositoryFromType(@NonNull final AbstractFilterDto filter) {
        return this.getRepositoryFromType(filter.getType());
    }

    /** @see AbstractFilterRepositoryProxy#deleteById(UUID) */
    public boolean deleteFilter(@NonNull final UUID id) {
        return this.identifierListFilterProxy.deleteById(id) || this.expertFilterProxy.deleteById(id);
    }

    /** @see AbstractFilterRepositoryProxy#deleteAllByIds(List) */
    public void deleteFilters(@NonNull final List<UUID> ids) {
        this.identifierListFilterProxy.deleteAllByIds(ids);
        this.expertFilterProxy.deleteAllByIds(ids);
    }

    /** @see AbstractFilterRepositoryProxy#deleteAll() */
    public void deleteAll() {
        this.identifierListFilterProxy.deleteAll();
        this.expertFilterProxy.deleteAll();
    }

    /** @see AbstractFilterRepositoryProxy#getFiltersAttributes() */
    @Transactional(readOnly = true)
    public Stream<FilterMetadataDto> getFiltersAttributes() {
        return Stream.concat(this.identifierListFilterProxy.getFiltersAttributes(), this.expertFilterProxy.getFiltersAttributes());
    }

    /** @see AbstractFilterRepositoryProxy#getFiltersAttributes(List) */
    @Transactional(readOnly = true)
    public Stream<FilterMetadataDto> getFiltersAttributes(@NonNull final List<UUID> ids) {
        return Stream.concat(this.identifierListFilterProxy.getFiltersAttributes(ids), this.expertFilterProxy.getFiltersAttributes(ids));
    }

    /** @see AbstractFilterRepositoryProxy#getFilters(List) */
    @Transactional(readOnly = true)
    public List<AbstractFilterDto> getFilters(@NonNull final List<UUID> ids) {
        return ListUtils.union(
            this.identifierListFilterProxy.getFilters(ids),
            this.expertFilterProxy.getFilters(ids)
        );
    }

    /** @see AbstractFilterRepositoryProxy#getFilter(UUID) */
    @Transactional(readOnly = true)
    public Optional<AbstractFilterDto> getFilter(@NonNull final UUID id) {
        return this.identifierListFilterProxy.getFilter(id).or(() -> this.expertFilterProxy.getFilter(id));
    }

    /** @see AbstractFilterRepositoryProxy#getFilter(UUID) */
    @Transactional(readOnly = true)
    public Optional<Filter> getFilterModel(@NonNull final UUID id) {
        return this.identifierListFilterProxy.getFilterModel(id).or(() -> this.expertFilterProxy.getFilterModel(id));
    }

    /** @see AbstractFilterRepositoryProxy#getFilters(List) */
    @Transactional(readOnly = true)
    public List<Filter> getFiltersModels(@NonNull final List<UUID> ids) {
        return ListUtils.union(
            this.identifierListFilterProxy.getFiltersModels(ids),
            this.expertFilterProxy.getFiltersModels(ids)
        );
    }
}
