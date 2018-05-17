/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
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

package org.hisp.dhis.android.core.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.hisp.dhis.android.core.utils.Utils.isNull;

public class ObjectStoreImpl<M extends BaseModel> implements ObjectStore<M> {
    protected final DatabaseAdapter databaseAdapter;
    protected final SQLiteStatement insertStatement;
    protected final SQLStatementBuilder builder;

    ObjectStoreImpl(DatabaseAdapter databaseAdapter, SQLiteStatement insertStatement, SQLStatementBuilder builder) {
        this.databaseAdapter = databaseAdapter;
        this.insertStatement = insertStatement;
        this.builder = builder;
    }

    @Override
    public void insert(@NonNull M m) throws RuntimeException {
        isNull(m);
        m.bindToStatement(insertStatement);
        Long insertedRowId = databaseAdapter.executeInsert(builder.tableName, insertStatement);
        insertStatement.clearBindings();
        if (insertedRowId == -1) {
            throw new RuntimeException("Nothing was inserted.");
        }
    }

    @Override
    public final int delete() {
        return databaseAdapter.delete(builder.tableName);
    }

    protected void executeUpdateDelete(SQLiteStatement statement) throws RuntimeException {
        int numberOfAffectedRows = databaseAdapter.executeUpdateDelete(builder.tableName, statement);
        statement.clearBindings();

        if (numberOfAffectedRows != 1) {
            throw new RuntimeException("Unexpected number of affected rows: " + numberOfAffectedRows);
        }
    }

    private void addAll(@NonNull CursorModelFactory<M> modelFactory,
                       @NonNull Collection<M> collection) throws RuntimeException {
        Cursor cursor = databaseAdapter.query(builder.selectAll());
        addObjectsToCollection(cursor, modelFactory, collection);
    }

    @Override
    public Set<M> selectAll(@NonNull CursorModelFactory<M> modelFactory) throws RuntimeException {
        Set<M> set = new HashSet<>();
        addAll(modelFactory, set);
        return set;
    }

    @Override
    public boolean deleteById(@NonNull M m) {
        return deleteWhereClause(BaseModel.Columns.ID + "='" + m.id() + "';");
    }

    private M selectOneWhere(@NonNull CursorModelFactory<M> modelFactory,
                               @NonNull String whereClause)
            throws RuntimeException {
        Cursor cursor = databaseAdapter.query(builder.selectWhereWithLimit(whereClause, 1));
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            return modelFactory.fromCursor(cursor);
        } else {
            return null;
        }
    }

    protected M popOneWhere(@NonNull CursorModelFactory<M> modelFactory,
                            @NonNull String whereClause) {
        M m = selectOneWhere(modelFactory, whereClause);
        if (m != null) {
            deleteById(m);
        }
        return m;
    }

    protected int countWhere(@NonNull String whereClause) {
        Cursor cursor = databaseAdapter.query(builder.countWhere(whereClause));
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    private void addObjectsToCollection(Cursor cursor, CursorModelFactory<M> modelFactory,
                                            Collection<M> collection) {
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    collection.add(modelFactory.fromCursor(cursor));
                }
                while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
    }

    protected boolean deleteWhereClause(String clause) {
        return databaseAdapter.database().delete(builder.tableName, clause, null) > 0;
    }
}