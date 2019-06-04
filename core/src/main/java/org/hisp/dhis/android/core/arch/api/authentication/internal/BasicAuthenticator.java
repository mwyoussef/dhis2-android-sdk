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

package org.hisp.dhis.android.core.arch.api.authentication.internal;

import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectWithoutUidStore;
import org.hisp.dhis.android.core.user.AuthenticatedUser;

import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import okhttp3.Request;
import okhttp3.Response;

final class BasicAuthenticator implements Authenticator {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BASIC_CREDENTIALS = "Basic %s";

    private final ObjectWithoutUidStore<AuthenticatedUser> authenticatedUserStore;

    BasicAuthenticator(@NonNull ObjectWithoutUidStore<AuthenticatedUser> authenticatedUserStore) {
        this.authenticatedUserStore = authenticatedUserStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String authorizationHeader = chain.request().header(AUTHORIZATION);
        if (authorizationHeader != null) {
            // authorization header has already been set
            return chain.proceed(chain.request());
        }

        AuthenticatedUser authenticatedUser = authenticatedUserStore.selectFirst();
        if (authenticatedUser == null || authenticatedUser.credentials() == null) {
            // proceed request if we do not
            // have any users authenticated
            return chain.proceed(chain.request());
        }

        // retrieve first user and pass in his / her credentials
        Request request = chain.request().newBuilder()
                .addHeader(AUTHORIZATION, String.format(Locale.US,
                        BASIC_CREDENTIALS, authenticatedUser.credentials()))
                .build();
        return chain.proceed(request);
    }
}