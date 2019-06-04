/*
 * Copyright (c) 2004-2019, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.dataset;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.call.factories.internal.QueryCallFactoryImpl;
import org.hisp.dhis.android.core.arch.call.fetchers.internal.CallFetcher;
import org.hisp.dhis.android.core.arch.call.internal.GenericCallData;
import org.hisp.dhis.android.core.arch.call.processors.internal.CallProcessor;
import org.hisp.dhis.android.core.arch.call.processors.internal.TransactionalNoResourceSyncCallProcessor;
import org.hisp.dhis.android.core.arch.handlers.internal.Handler;

import javax.inject.Inject;

import dagger.Reusable;
import retrofit2.Call;

import static org.hisp.dhis.android.core.utils.Utils.commaSeparatedCollectionValues;

@Reusable
final class DataSetCompleteRegistrationCallFactory extends QueryCallFactoryImpl<DataSetCompleteRegistration,
        DataSetCompleteRegistrationQuery> {

    private final Handler<DataSetCompleteRegistration> handler;
    private final DataSetCompleteRegistrationService service;

    @Inject
    DataSetCompleteRegistrationCallFactory(GenericCallData genericCallData,
                                           APICallExecutor apiCallExecutor,
                                           Handler<DataSetCompleteRegistration> handler,
                                           DataSetCompleteRegistrationService service) {
        super(genericCallData, apiCallExecutor);
        this.handler = handler;
        this.service = service;
    }

    @Override
    protected CallFetcher<DataSetCompleteRegistration> fetcher(
            final DataSetCompleteRegistrationQuery dataSetCompleteRegistrationQuery) {

        return new DataSetCompleteRegistrationCallFetcher(
                dataSetCompleteRegistrationQuery.dataSetUids(),
                dataSetCompleteRegistrationQuery.periodIds(),
                dataSetCompleteRegistrationQuery.rootOrgUnitUids(),
                apiCallExecutor) {

            @Override
            protected Call<DataSetCompleteRegistrationPayload> getCall(
                    DataSetCompleteRegistrationQuery dataSetCompleteRegistrationQuery) {
                return service.getDataSetCompleteRegistrations(
                        DataSetCompleteRegistrationFields.allFields,
                        commaSeparatedCollectionValues(dataSetCompleteRegistrationQuery.dataSetUids()),
                        commaSeparatedCollectionValues(dataSetCompleteRegistrationQuery.periodIds()),
                        commaSeparatedCollectionValues(dataSetCompleteRegistrationQuery.rootOrgUnitUids()),
                        Boolean.TRUE,
                        Boolean.FALSE);
            }
        };
    }

    @Override
    protected CallProcessor<DataSetCompleteRegistration> processor(DataSetCompleteRegistrationQuery query) {
        return new TransactionalNoResourceSyncCallProcessor<>(data.databaseAdapter(), handler);
    }
}
