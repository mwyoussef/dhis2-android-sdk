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

package org.hisp.dhis.android.testapp.trackedentity;

import com.google.common.collect.Lists;

import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.BaseNameableObject;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(D2JunitRunner.class)
public class TrackedEntityCollectionRepositoryMockIntegrationShould extends BaseMockIntegrationTestFullDispatcher {

    @Test
    public void find_all() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(2));
    }

    @Test
    public void filter_by_uid() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byUid().eq("nWrB0TfWlvD")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_created() throws ParseException {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byCreated().eq(BaseNameableObject.DATE_FORMAT.parse("2019-01-10T13:40:27.987"))
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_lastUpdated() throws ParseException {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byLastUpdated().eq(BaseNameableObject.DATE_FORMAT.parse("2018-01-10T13:40:28.592"))
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_created_at_client() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byCreatedAtClient().eq("2019-01-22T18:38:15.845")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_last_updated_at_client() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byLastUpdatedAtClient().eq("2019-01-22T18:38:15.845")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_organisation_unit() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byOrganisationUnitUid().eq("DiszpKrYNg8")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(2));
    }

    @Test
    public void filter_by_tracked_entity_type() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byTrackedEntityType().eq("nEenWmSyUEp")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(2));
    }

    @Test
    public void filter_by_geometry_type() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byGeometryType().eq(FeatureType.POINT)
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_geometry_coordinates() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byGeometryCoordinates().eq("[9.0, 9.0]")
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_state() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byState().eq(State.SYNCED)
                        .blockingGet();

        // TODO set to assertThat(trackedEntityInstances.size(), is(2)); after moving write tests to another db
        assertThat(trackedEntityInstances.size(), is(1));
    }

    @Test
    public void filter_by_deleted() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byDeleted().isFalse()
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(2));
    }

    @Test
    public void filter_by_program_uids() {
        List<TrackedEntityInstance> trackedEntityInstances =
                d2.trackedEntityModule().trackedEntityInstances()
                        .byProgramUids(Lists.newArrayList("lxAQ7Zs9VYR"))
                        .blockingGet();

        assertThat(trackedEntityInstances.size(), is(2));
    }

    @Test
    public void order_by_created() {
        List<TrackedEntityInstance> trackedEntityInstances = d2.trackedEntityModule().trackedEntityInstances()
                        .orderByCreated(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();

        assertThat(trackedEntityInstances.get(0).uid(), is("nWrB0TfWlvD"));
        assertThat(trackedEntityInstances.get(1).uid(), is("nWrB0TfWlvh"));
    }

    @Test
    public void order_by_created_at_client() {
        List<TrackedEntityInstance> trackedEntityInstances = d2.trackedEntityModule().trackedEntityInstances()
                        .orderByCreatedAtClient(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();

        assertThat(trackedEntityInstances.get(0).uid(), is("nWrB0TfWlvh"));
        assertThat(trackedEntityInstances.get(1).uid(), is("nWrB0TfWlvD"));
    }

    @Test
    public void order_by_last_updated() {
        List<TrackedEntityInstance> trackedEntityInstances = d2.trackedEntityModule().trackedEntityInstances()
                        .orderByLastUpdated(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();

        assertThat(trackedEntityInstances.get(0).uid(), is("nWrB0TfWlvD"));
        assertThat(trackedEntityInstances.get(1).uid(), is("nWrB0TfWlvh"));
    }

    @Test
    public void order_by_last_updated_at_client() {
        List<TrackedEntityInstance> trackedEntityInstances = d2.trackedEntityModule().trackedEntityInstances()
                        .orderByLastUpdatedAtClient(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();

        assertThat(trackedEntityInstances.get(0).uid(), is("nWrB0TfWlvh"));
        assertThat(trackedEntityInstances.get(1).uid(), is("nWrB0TfWlvD"));
    }
}