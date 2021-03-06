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

package org.hisp.dhis.android.core.common.internal;

import androidx.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStore;
import org.hisp.dhis.android.core.enrollment.internal.EnrollmentStoreImpl;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.internal.EventStore;
import org.hisp.dhis.android.core.event.internal.EventStoreImpl;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceStore;
import org.hisp.dhis.android.core.trackedentity.internal.TrackedEntityInstanceStoreImpl;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class DataStatePropagatorIntegrationShould extends BaseMockIntegrationTestFullDispatcher {

    private DataStatePropagator propagator;
    private TrackedEntityInstanceStore trackedEntityInstanceStore;
    private EnrollmentStore enrollmentStore;
    private EventStore eventStore;

    @Before
    public void setUp() throws IOException {
        this.trackedEntityInstanceStore = TrackedEntityInstanceStoreImpl.create(d2.databaseAdapter());
        this.enrollmentStore = EnrollmentStoreImpl.create(d2.databaseAdapter());
        this.eventStore = EventStoreImpl.create(d2.databaseAdapter());
        this.propagator = new DataStatePropagatorImpl(
                trackedEntityInstanceStore,
                enrollmentStore,
                eventStore);
    }

    @Test
    public void set_parent_state_to_update_if_has_synced_state() throws D2Error {
        assertThatSetTeiToUpdateWhenEnrollmentPropagation(State.SYNCED);
        assertThatSetTeiToUpdateWhenEventPropagation(State.SYNCED);
        assertThatSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State.SYNCED);
    }

    @Test
    public void set_parent_state_to_update_if_has_to_update_state() throws D2Error {
        assertThatSetTeiToUpdateWhenEnrollmentPropagation(State.TO_UPDATE);
        assertThatSetTeiToUpdateWhenEventPropagation(State.TO_UPDATE);
        assertThatSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State.TO_UPDATE);
    }

    @Test
    public void set_parent_state_to_update_if_has_error_state() throws D2Error {
        assertThatSetTeiToUpdateWhenEnrollmentPropagation(State.ERROR);
        assertThatSetTeiToUpdateWhenEventPropagation(State.ERROR);
        assertThatSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State.ERROR);
    }

    @Test
    public void set_parent_state_to_update_if_has_warning_state() throws D2Error {
        assertThatSetTeiToUpdateWhenEnrollmentPropagation(State.WARNING);
        assertThatSetTeiToUpdateWhenEventPropagation(State.WARNING);
        assertThatSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State.WARNING);
    }

    @Test
    public void do_not_set_parent_state_to_update_if_has_to_post_state() throws D2Error {
        assertThatDoNotSetTeiToUpdateWhenEnrollmentPropagation(State.TO_POST);
        assertThatDoNotSetTeiToUpdateWhenEventPropagation(State.TO_POST);
        assertThatDoNotSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State.TO_POST);
    }

    @Test
    public void do_not_fail_with_events_without_registration() throws D2Error {
        String eventUid = d2.eventModule().events().blockingAdd(sampleEventProjection(null));

        assertThat(eventStore.selectByUid(eventUid).state(), is(State.TO_POST));
        eventStore.delete(eventUid);
    }

    @Test
    public void reset_enrollment_and_event_states_if_uploading() throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        String enrolmentUid1 = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));
        String enrolmentUid2 = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));

        String eventUid1 = d2.eventModule().events().blockingAdd(sampleEventProjection(enrolmentUid1));
        String eventUid2 = d2.eventModule().events().blockingAdd(sampleEventProjection(enrolmentUid1));
        String eventUid3 = d2.eventModule().events().blockingAdd(sampleEventProjection(enrolmentUid2));

        enrollmentStore.setState(enrolmentUid1, State.UPLOADING);
        eventStore.setState(eventUid1, State.UPLOADING);

        propagator.resetUploadingEnrollmentAndEventStates(teiUid);

        assertThat(enrollmentStore.getState(enrolmentUid1), is(State.TO_UPDATE));
        assertThat(enrollmentStore.getState(enrolmentUid2), is(State.TO_POST));

        assertThat(eventStore.getState(eventUid1), is(State.TO_UPDATE));
        assertThat(eventStore.getState(eventUid2), is(State.TO_POST));
        assertThat(eventStore.getState(eventUid3), is(State.TO_POST));

        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatSetTeiToUpdateWhenEnrollmentPropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        trackedEntityInstanceStore.setState(teiUid, state);

        propagator.propagateEnrollmentUpdate(Enrollment.builder().uid("uid").trackedEntityInstance(teiUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(State.TO_UPDATE));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatDoNotSetTeiToUpdateWhenEnrollmentPropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        trackedEntityInstanceStore.setState(teiUid, state);

        propagator.propagateEnrollmentUpdate(Enrollment.builder().uid("uid").trackedEntityInstance(teiUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(state));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatSetTeiToUpdateWhenEventPropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        String enrolmentUid = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));

        trackedEntityInstanceStore.setState(teiUid, state);
        enrollmentStore.setState(enrolmentUid, state);

        propagator.propagateEventUpdate(Event.builder().uid("uid").enrollment(enrolmentUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(State.TO_UPDATE));
        assertThat(enrollmentStore.selectByUid(enrolmentUid).state(), is(State.TO_UPDATE));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatDoNotSetTeiToUpdateWhenEventPropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        String enrolmentUid = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));

        trackedEntityInstanceStore.setState(teiUid, state);
        enrollmentStore.setState(enrolmentUid, state);

        propagator.propagateEventUpdate(Event.builder().uid("uid").enrollment(enrolmentUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(state));
        assertThat(enrollmentStore.selectByUid(enrolmentUid).state(), is(state));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        String enrolmentUid = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));
        String eventUid = d2.eventModule().events().blockingAdd(sampleEventProjection(enrolmentUid));


        trackedEntityInstanceStore.setState(teiUid, state);
        enrollmentStore.setState(enrolmentUid, state);
        eventStore.setState(eventUid, state);

        propagator.propagateTrackedEntityDataValueUpdate(TrackedEntityDataValue.builder().event(eventUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(State.TO_UPDATE));
        assertThat(enrollmentStore.selectByUid(enrolmentUid).state(), is(State.TO_UPDATE));
        assertThat(eventStore.selectByUid(eventUid).state(), is(State.TO_UPDATE));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private void assertThatDoNotSetTeiToUpdateWhenTrackedEntityDataValuePropagation(State state) throws D2Error {
        String teiUid = d2.trackedEntityModule().trackedEntityInstances().blockingAdd(sampleTEIProjection());
        String enrolmentUid = d2.enrollmentModule().enrollments().blockingAdd(sampleEnrollmentProjection(teiUid));
        String eventUid = d2.eventModule().events().blockingAdd(sampleEventProjection(enrolmentUid));

        trackedEntityInstanceStore.setState(teiUid, state);
        enrollmentStore.setState(enrolmentUid, state);
        eventStore.setState(eventUid, state);

        propagator.propagateTrackedEntityDataValueUpdate(TrackedEntityDataValue.builder().event(eventUid).build());

        assertThat(trackedEntityInstanceStore.selectByUid(teiUid).state(), is(state));
        assertThat(enrollmentStore.selectByUid(enrolmentUid).state(), is(state));
        assertThat(eventStore.selectByUid(eventUid).state(), is(state));
        trackedEntityInstanceStore.delete(teiUid);
    }

    private TrackedEntityInstanceCreateProjection sampleTEIProjection() {
        return TrackedEntityInstanceCreateProjection.create("DiszpKrYNg8", "nEenWmSyUEp");
    }

    private EnrollmentCreateProjection sampleEnrollmentProjection(String teiUid) {
        return EnrollmentCreateProjection.create("DiszpKrYNg8", "lxAQ7Zs9VYR", teiUid);
    }

    private EventCreateProjection sampleEventProjection(String enrollmentUid) {
        return EventCreateProjection.create(enrollmentUid, "lxAQ7Zs9VYR", "dBwrot7S420",
                "DiszpKrYNg8", "bRowv6yZOF2");
    }
}