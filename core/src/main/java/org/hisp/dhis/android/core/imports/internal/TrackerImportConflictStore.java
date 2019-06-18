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

package org.hisp.dhis.android.core.imports.internal;

import org.hisp.dhis.android.core.arch.db.stores.binders.internal.StatementBinder;
import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectStore;
import org.hisp.dhis.android.core.arch.db.stores.internal.StoreFactory;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.imports.TrackerImportConflictTableInfo;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

public final class TrackerImportConflictStore {

    private static final StatementBinder<TrackerImportConflict> BINDER = (o, sqLiteStatement) -> {
        sqLiteBind(sqLiteStatement, 1, o.conflict());
        sqLiteBind(sqLiteStatement, 2, o.value());
        sqLiteBind(sqLiteStatement, 3, o.trackedEntityInstance());
        sqLiteBind(sqLiteStatement, 4, o.enrollment());
        sqLiteBind(sqLiteStatement, 5, o.event());
        sqLiteBind(sqLiteStatement, 6, o.tableReference());
        sqLiteBind(sqLiteStatement, 7, o.errorCode());
        sqLiteBind(sqLiteStatement, 8, o.status());
        sqLiteBind(sqLiteStatement, 9, o.created());
    };

    private TrackerImportConflictStore() {
    }

    public static ObjectStore<TrackerImportConflict> create(DatabaseAdapter databaseAdapter) {
        return StoreFactory.objectStore(databaseAdapter, TrackerImportConflictTableInfo.TABLE_INFO, BINDER,
                TrackerImportConflict::create);
    }
}