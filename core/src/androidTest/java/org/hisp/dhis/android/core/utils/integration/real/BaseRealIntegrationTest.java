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

package org.hisp.dhis.android.core.utils.integration.real;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import com.facebook.stetho.Stetho;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.call.internal.GenericCallData;
import org.hisp.dhis.android.core.arch.d2.internal.D2DIComponent;
import org.hisp.dhis.android.core.arch.db.access.DatabaseAdapter;
import org.hisp.dhis.android.core.arch.db.access.internal.DatabaseAdapterFactory;
import org.hisp.dhis.android.core.arch.storage.internal.AndroidSecureStore;
import org.hisp.dhis.android.core.arch.storage.internal.Credentials;
import org.hisp.dhis.android.core.arch.storage.internal.CredentialsSecureStoreImpl;
import org.hisp.dhis.android.core.arch.storage.internal.ObjectSecureStore;
import org.hisp.dhis.android.core.arch.storage.internal.SecureStore;
import org.hisp.dhis.android.core.data.server.RealServerMother;
import org.hisp.dhis.android.core.resource.internal.ResourceHandler;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

public abstract class BaseRealIntegrationTest {
    private DatabaseAdapter databaseAdapter;

    protected Date serverDate = new Date();
    protected ResourceHandler resourceHandler;
    private SecureStore secureStore;
    protected ObjectSecureStore<Credentials> credentialsSecureStore;

    protected String username = RealServerMother.username;
    protected String password = RealServerMother.password;
    protected String url = RealServerMother.url;

    @Before
    public void setUp() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();

        databaseAdapter = DatabaseAdapterFactory.getDatabaseAdapter(context);
        DatabaseAdapterFactory.createOrOpenDatabase(databaseAdapter, null, false);
        secureStore = new AndroidSecureStore(context);
        credentialsSecureStore = new CredentialsSecureStoreImpl(secureStore);
        resourceHandler = ResourceHandler.create(databaseAdapter);
        resourceHandler.setServerDate(serverDate);
        Stetho.initializeWithDefaults(context);
    }

    @After
    public void tearDown() throws IOException {
        assertThat(databaseAdapter).isNotNull();
        try {
            databaseAdapter.close();
        } catch (Exception e) {
            // Otherwise SQLiteException: unable to close due to unfinalized statements or unfinished backups:
            // sqlite3_close() failed with SQL cipher
            // TODO Fix in the SDK, otherwise it will throw the errors in production
        }
    }

    protected DatabaseAdapter databaseAdapter() {
        return databaseAdapter;
    }

    protected GenericCallData getGenericCallData(D2 d2) {
        return GenericCallData.create(
                databaseAdapter(), d2.retrofit(), resourceHandler, d2.systemInfoModule().versionManager());
    }

    protected D2DIComponent getD2DIComponent(D2 d2) {
        return D2DIComponent.create(InstrumentationRegistry.getTargetContext().getApplicationContext(), d2.retrofit(),
                databaseAdapter, secureStore);
    }
}
