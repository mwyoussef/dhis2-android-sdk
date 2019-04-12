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

package org.hisp.dhis.android.core.arch.api.executors;

import org.hisp.dhis.android.core.ObjectMapperFactory;
import org.hisp.dhis.android.core.common.ObjectStore;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.maintenance.D2ErrorStore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public final class APICallExecutorImpl implements APICallExecutor {

    private final ObjectStore<D2Error> errorStore;
    private final APIErrorMapper errorMapper = new APIErrorMapper();

    public APICallExecutorImpl(ObjectStore<D2Error> errorStore) {
        this.errorStore = errorStore;
    }

    @Override
    public <P> List<P> executePayloadCall(Call<Payload<P>> call) throws D2Error {
        D2Error.Builder errorBuilder = errorMapper.getCollectionErrorBuilder(call);

        try {
            Response<Payload<P>> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().items();
            } else {
                throw storeAndReturn(errorMapper.responseException(errorBuilder, response));
            }
        } catch (Throwable t) {
            throw storeAndReturn(errorMapper.mapRetrofitException(t, errorBuilder));
        }
    }

    @Override
    public <P> P executeObjectCall(Call<P> call) throws D2Error {
        return executeObjectCallInternal(call, new ArrayList<>(), null, null, false);
    }

    @Override
    public <P> P executeObjectCallWithAcceptedErrorCodes(Call<P> call, List<Integer> acceptedErrorCodes,
                                                         Class<P> errorClass) throws D2Error {
        return executeObjectCallInternal(call, acceptedErrorCodes, errorClass, null, false);
    }

    @Override
    public <P> P executeObjectCallWithErrorCatcher(Call<P> call, APICallErrorCatcher errorCatcher)
            throws D2Error {
        return executeObjectCallInternal(call, new ArrayList<>(), null, errorCatcher, false);
    }

    @Override
    public Unit executeObjectCallWithEmptyResponse(Call<Unit> call) throws D2Error {
        return executeObjectCallInternal(call, new ArrayList<>(), null, null, true);
    }

    private <P> P executeObjectCallInternal(Call<P> call,
                                            List<Integer> acceptedErrorCodes,
                                            Class<P> errorClass,
                                            APICallErrorCatcher errorCatcher,
                                            boolean emptyBodyExpected) throws D2Error {

        D2Error.Builder errorBuilder = errorMapper.getObjectErrorBuilder(call);

        try {
            Response<P> response = call.execute();
            if (response.isSuccessful()) {
                return processSuccessfulResponse(errorBuilder, response, emptyBodyExpected);
            } else if (errorClass != null && acceptedErrorCodes.contains(response.code())) {
                return ObjectMapperFactory.objectMapper().readValue(response.errorBody().string(), errorClass);
            } else if (errorCatcher != null) {
                D2ErrorCode d2ErrorCode = errorCatcher.catchError(response);

                if (d2ErrorCode != null) {
                    D2Error d2error = errorMapper.responseException(errorBuilder, response, d2ErrorCode);

                    if (errorCatcher.mustBeStored()) {
                        throw storeAndReturn(d2error);
                    } else {
                        throw d2error;
                    }
                }
            }
            throw storeAndReturn(errorMapper.responseException(errorBuilder, response));
        } catch (Throwable t) {
            throw storeAndReturn(errorMapper.mapRetrofitException(t, errorBuilder));
        }
    }

    private <P> P processSuccessfulResponse(D2Error.Builder errorBuilder, Response<P> response,
                                            boolean emptyBodyExpected) throws D2Error {
        if (emptyBodyExpected) {
            return null;
        } else if (response.body() == null) {
            throw storeAndReturn(errorMapper.responseException(errorBuilder, response));
        } else {
            return response.body();
        }
    }

    private D2Error storeAndReturn(D2Error error) {
        errorStore.insert(error);
        return error;
    }

    public static APICallExecutor create(DatabaseAdapter databaseAdapter) {
        return new APICallExecutorImpl(D2ErrorStore.create(databaseAdapter));
    }
}