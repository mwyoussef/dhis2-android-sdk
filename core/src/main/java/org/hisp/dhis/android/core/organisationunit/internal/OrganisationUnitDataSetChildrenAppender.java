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

package org.hisp.dhis.android.core.organisationunit.internal;

import org.hisp.dhis.android.core.arch.db.stores.internal.LinkModelChildStore;
import org.hisp.dhis.android.core.arch.db.stores.internal.StoreFactory;
import org.hisp.dhis.android.core.arch.db.stores.projections.internal.LinkTableChildProjection;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetOrganisationUnitLinkTableInfo;
import org.hisp.dhis.android.core.dataset.DataSetTableInfo;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

final class OrganisationUnitDataSetChildrenAppender extends ChildrenAppender<OrganisationUnit> {

    private static final LinkTableChildProjection CHILD_PROJECTION = new LinkTableChildProjection(
            DataSetTableInfo.TABLE_INFO,
            DataSetOrganisationUnitLinkTableInfo.Columns.ORGANISATION_UNIT,
            DataSetOrganisationUnitLinkTableInfo.Columns.DATA_SET);

    private final LinkModelChildStore<OrganisationUnit, DataSet> linkModelChildStore;

    private OrganisationUnitDataSetChildrenAppender(
            LinkModelChildStore<OrganisationUnit, DataSet> linkModelChildStore) {
        this.linkModelChildStore = linkModelChildStore;
    }

    @Override
    protected OrganisationUnit appendChildren(OrganisationUnit organisationUnit) {
        OrganisationUnit.Builder builder = organisationUnit.toBuilder();
        builder.dataSets(linkModelChildStore.getChildren(organisationUnit));
        return builder.build();
    }

    static ChildrenAppender<OrganisationUnit> create(DatabaseAdapter databaseAdapter) {
        return new OrganisationUnitDataSetChildrenAppender(
                StoreFactory.linkModelChildStore(
                        databaseAdapter,
                        DataSetOrganisationUnitLinkTableInfo.TABLE_INFO,
                        CHILD_PROJECTION,
                        DataSet::create
                )
        );
    }
}