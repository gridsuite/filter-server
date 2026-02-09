package org.gridsuite.filter.server;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.network.store.client.NetworkStoreService;
import com.powsybl.network.store.client.PreloadingStrategy;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.globalfilter.AbstractGlobalFilterService;
import org.gridsuite.filter.globalfilter.GlobalFilter;
import org.gridsuite.filter.utils.EquipmentType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GlobalFilterService extends AbstractGlobalFilterService {
    private final NetworkStoreService networkStoreService;
    private final RepositoryService repositoriesService;

    /** {@inheritDoc} */
    @Override
    protected Network getNetwork(@NonNull final UUID networkUuid, @NonNull final String variantId) {
        try {
            Network network = networkStoreService.getNetwork(networkUuid, PreloadingStrategy.COLLECTION);
            network.getVariantManager().setWorkingVariant(variantId);
            return network;
        } catch (final PowsyblException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<AbstractFilter> getFilters(@NonNull final List<UUID> filtersUuids) {
        return this.repositoriesService.getFilters(filtersUuids);
    }

    /* Expose it publicly */
    /** {@inheritDoc} */
    @Override
    public List<String> getFilteredIds(@NonNull final UUID networkUuid, @NonNull final String variantId,
                                       @NonNull final GlobalFilter globalFilter,
                                       @NonNull final List<EquipmentType> equipmentTypes) {
        return super.getFilteredIds(networkUuid, variantId, globalFilter, equipmentTypes);
    }
}
