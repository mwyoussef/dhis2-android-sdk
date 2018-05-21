package org.hisp.dhis.android.core.trackedentity;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.APICallExecutor;
import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.common.SyncCall;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;

import java.util.Collection;
import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class TrackedEntityInstanceByUidEndPointCall extends SyncCall<List<TrackedEntityInstance>> {

    private final DatabaseAdapter databaseAdapter;
    private final Retrofit retrofit;

    private final TrackedEntityInstanceService trackedEntityInstanceService;
    private final Collection<String> trackedEntityInstanceUids;

    TrackedEntityInstanceByUidEndPointCall(
            @NonNull DatabaseAdapter databaseAdapter,
            @NonNull Retrofit retrofit,
            @NonNull TrackedEntityInstanceService trackedEntityInstanceService,
            @NonNull Collection<String> trackedEntityInstanceUids) {
        this.databaseAdapter = databaseAdapter;
        this.retrofit = retrofit;

        this.trackedEntityInstanceService = trackedEntityInstanceService;
        this.trackedEntityInstanceUids = trackedEntityInstanceUids;
    }

    @Override
    public List<TrackedEntityInstance> call() throws D2CallException {
        super.setExecuted();

        Call<Payload<TrackedEntityInstance>> call =
                trackedEntityInstanceService.getTrackedEntityInstancesById(
                        TrackedEntityInstance.uid.in(trackedEntityInstanceUids),
                        TrackedEntityInstance.allFields, true);

        List<TrackedEntityInstance> teis = new APICallExecutor().executePayloadCall(call);
        Transaction transaction = databaseAdapter.beginNewTransaction();

        TrackedEntityInstancePersistenceCall.create(databaseAdapter, retrofit, teis).call();

        transaction.setSuccessful();
        transaction.end();
        return teis;
    }

    public static SyncCall<List<TrackedEntityInstance>> create(DatabaseAdapter databaseAdapter,
                                                               Retrofit retrofit,
                                                               Collection<String> trackedEntityInstanceUids) {
        return new TrackedEntityInstanceByUidEndPointCall(
                databaseAdapter,
                retrofit,
                retrofit.create(TrackedEntityInstanceService.class),
                trackedEntityInstanceUids
        );
    }
}
