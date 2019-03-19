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

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.arch.api.executors.APICallExecutor;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.imports.DataValueImportSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dagger.Reusable;

@Reusable
final class DataSetCompleteRegistrationPostCall implements Callable<DataValueImportSummary> {

    private final DataSetCompleteRegistrationService dataSetCompleteRegistrationService;
    private final DataSetCompleteRegistrationStore  dataSetCompleteRegistrationStore;
    private final APICallExecutor apiCallExecutor;

    @Inject
    DataSetCompleteRegistrationPostCall(
            @NonNull DataSetCompleteRegistrationService dataSetCompleteRegistrationService,
            @NonNull DataSetCompleteRegistrationStore dataSetCompleteRegistrationStore,
            @NonNull APICallExecutor apiCallExecutor) {

        this.dataSetCompleteRegistrationService = dataSetCompleteRegistrationService;
        this.dataSetCompleteRegistrationStore = dataSetCompleteRegistrationStore;
        this.apiCallExecutor = apiCallExecutor;
    }

    @Override
    public DataValueImportSummary call() throws Exception {
        List<DataSetCompleteRegistration> toPostDataSetCompleteRegistrations = new ArrayList<>();

        appendPostableDataValues(toPostDataSetCompleteRegistrations);
        appendUpdatableDataValues(toPostDataSetCompleteRegistrations);

        if (toPostDataSetCompleteRegistrations.isEmpty()) {
            return DataValueImportSummary.EMPTY;
        }

        DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload
                = new DataSetCompleteRegistrationPayload(toPostDataSetCompleteRegistrations);

        DataValueImportSummary dataValueImportSummary = apiCallExecutor.executeObjectCall(
                dataSetCompleteRegistrationService.postDataSetCompleteRegistrations(
                        dataSetCompleteRegistrationPayload));

        handleImportSummary(dataSetCompleteRegistrationPayload, dataValueImportSummary);

        return dataValueImportSummary;
    }

    private void appendPostableDataValues(Collection<DataSetCompleteRegistration> dataSetCompleteRegistrations) {
        dataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getDataSetCompleteRegistrationsWithState(State.TO_POST));
    }

    private void appendUpdatableDataValues(Collection<DataSetCompleteRegistration> dataSetCompleteRegistrations) {
        dataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getDataSetCompleteRegistrationsWithState(State.TO_UPDATE));
    }

    private void handleImportSummary(DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload,
                                     DataValueImportSummary dataValueImportSummary) {

        DataSetCompleteRegistrationImportHandler dataSetCompleteRegistrationImportHandler =
                new DataSetCompleteRegistrationImportHandler(dataSetCompleteRegistrationStore);

        dataSetCompleteRegistrationImportHandler.handleImportSummary(
                dataSetCompleteRegistrationPayload, dataValueImportSummary);
    }
}
