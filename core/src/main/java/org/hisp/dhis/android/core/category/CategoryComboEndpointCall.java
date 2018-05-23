package org.hisp.dhis.android.core.category;


import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.calls.Call;
import org.hisp.dhis.android.core.common.GenericCallData;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.common.SimpleCallFactory;
import org.hisp.dhis.android.core.common.SyncCall;
import org.hisp.dhis.android.core.data.api.Fields;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceModel;

import java.util.Date;
import java.util.List;

import retrofit2.Response;

public final class CategoryComboEndpointCall extends SyncCall<Response<Payload<CategoryCombo>>> {

    private final CategoryComboQuery query;
    private final CategoryComboService categoryComboService;
    private final ResponseValidator<CategoryCombo> responseValidator;
    private final CategoryComboHandler handler;
    private final ResourceHandler resourceHandler;
    private final DatabaseAdapter databaseAdapter;
    private final Date serverDate;

    private CategoryComboEndpointCall(CategoryComboQuery query,
            CategoryComboService categoryComboService,
            ResponseValidator<CategoryCombo> responseValidator,
            CategoryComboHandler handler,
            ResourceHandler resourceHandler,
            DatabaseAdapter databaseAdapter, Date serverDate) {
        this.query = query;
        this.categoryComboService = categoryComboService;
        this.responseValidator = responseValidator;
        this.handler = handler;
        this.resourceHandler = resourceHandler;
        this.databaseAdapter = databaseAdapter;
        this.serverDate = new Date(serverDate.getTime());
    }

    @Override
    public Response<Payload<CategoryCombo>> call() throws Exception {
        super.setExecuted();

        Response<Payload<CategoryCombo>> response = categoryComboService.getCategoryCombos(getFields(),
                query.paging(),
                query.page(), query.pageSize())
                .execute();

        if (responseValidator.isValid(response)) {
            List<CategoryCombo> categoryCombos = response.body().items();

            handle(categoryCombos);
        }

        return response;
    }

    private void handle(List<CategoryCombo> categoryCombos) {
        Transaction transaction = databaseAdapter.beginNewTransaction();

        try {
            for (CategoryCombo categoryCombo : categoryCombos) {
                handler.handle(categoryCombo);
            }
            resourceHandler.handleResource(ResourceModel.Type.CATEGORY_COMBO, serverDate);
            transaction.setSuccessful();
        } finally {
            transaction.end();
        }
    }

    @NonNull
    private Fields<CategoryCombo> getFields() {

        return Fields.<CategoryCombo>builder().fields(CategoryCombo.uid, CategoryCombo.code,
                CategoryCombo.name, CategoryCombo.displayName,
                CategoryCombo.created, CategoryCombo.lastUpdated, CategoryCombo.deleted,
                CategoryCombo.displayName, CategoryCombo.isDefault, CategoryCombo.categories,
                CategoryCombo.categoryOptionCombos.with(CategoryOptionCombo.uid,
                        CategoryOptionCombo.code,
                        CategoryOptionCombo.name,
                        CategoryOptionCombo.displayName,
                        CategoryOptionCombo.created,
                        CategoryOptionCombo.lastUpdated,
                        CategoryOptionCombo.deleted,
                        CategoryOptionCombo.categoryCombo.with(CategoryCombo.uid),
                        CategoryOptionCombo.displayName,
                        CategoryOptionCombo.categoryOptions.with(
                                CategoryOption.uid
                        )))
                .build();
    }

    public static final SimpleCallFactory<Payload<CategoryCombo>> FACTORY
            = new SimpleCallFactory<Payload<CategoryCombo>>() {

        @Override
        public Call<Response<Payload<CategoryCombo>>> create(GenericCallData genericCallData) {
            return new CategoryComboEndpointCall(
                    CategoryComboQuery.defaultQuery(),
                    genericCallData.retrofit().create(CategoryComboService.class),
                    new ResponseValidator<CategoryCombo>(),
                    CategoryComboHandler.create(genericCallData.databaseAdapter()),
                    ResourceHandler.create(genericCallData.databaseAdapter()),
                    genericCallData.databaseAdapter(),
                    genericCallData.serverDate()
            );
        }
    };
}