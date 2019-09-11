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

package org.hisp.dhis.android.testapp.fileresource;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.apache.commons.io.FileUtils;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.fileresource.RandomGeneratedInputStream;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.fileresource.FileResourceTableInfo;
import org.hisp.dhis.android.core.fileresource.internal.FileResourceUtil;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.utils.integration.mock.BaseMockIntegrationTestFullDispatcher;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.hisp.dhis.android.core.wipe.internal.TableWiper;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(D2JunitRunner.class)
public class FileResourceCollectionRepositoryMockIntegrationShould extends BaseMockIntegrationTestFullDispatcher {

    @Test
    public void find_all() throws D2Error, IOException {
        cleanFileResources();
        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void filter_by_uid() throws D2Error, IOException {
        cleanFileResources();
        String fileUid = d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byUid().eq(fileUid)
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void filter_by_name() throws D2Error, IOException {
        cleanFileResources();
        String fileUid = d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byName().eq(fileUid)
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }
    @Test
    public void filter_by_last_updated() throws D2Error, IOException, ParseException {
        cleanFileResources();
        String BEFORE_DATE = "2007-12-24T12:24:25.203";
        Date created = BaseIdentifiableObject.DATE_FORMAT.parse(BEFORE_DATE);

        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byLastUpdated().after(created)
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void filter_by_content_type() throws D2Error, IOException {
        cleanFileResources();
        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byContentType().eq("image/png")
                        .blockingGet();

        assertThat(fileResources.size(), is(0));
    }

    @Test
    public void filter_by_path() throws D2Error, IOException {
        cleanFileResources();
        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byPath().eq("/data/user/0/org.hisp.dhis.android.test/files/sdk_resources")
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void filter_by_state() throws D2Error, IOException {
        cleanFileResources();
        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byState().eq(State.TO_POST)
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void filter_by_content_length() throws D2Error, IOException {
        cleanFileResources();
        d2.fileResourceModule().fileResources.blockingAdd(getFile());
        List<FileResource> fileResources =
                d2.fileResourceModule().fileResources
                        .byContentLength().eq(1024L)
                        .blockingGet();

        assertThat(fileResources.size(), is(1));
    }

    @Test
    public void add_fileResources_to_the_repository() throws D2Error, IOException {
        cleanFileResources();
        List<FileResource> fileResources1 = d2.fileResourceModule().fileResources.blockingGet();
        assertThat(fileResources1.size(), is(0));

        File file = getFile();
        assertThat(file.exists(), is(true));

        String fileResourceUid = d2.fileResourceModule().fileResources.blockingAdd(file);

        List<FileResource> fileResources2 = d2.fileResourceModule().fileResources.blockingGet();
        assertThat(fileResources2.size(), is(1));

        FileResource fileResource = d2.fileResourceModule().fileResources.uid(fileResourceUid).blockingGet();
        assertThat(fileResource.uid(), is(fileResourceUid));

        File savedFile = new File(fileResource.path(), fileResource.uid());
        assertThat(savedFile.exists(), is(true));
    }

    private File getFile() {
        InputStream inputStream = new RandomGeneratedInputStream(1024);
        Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        File destinationFile = new File(FileResourceUtil.getFileResourceDirectory(context), "file1");
        return FileResourceUtil.writeInputStream(inputStream, destinationFile, 1024);
    }

    private void cleanFileResources() throws IOException {
        Context context = InstrumentationRegistry.getTargetContext().getApplicationContext();
        FileUtils.cleanDirectory(FileResourceUtil.getFileResourceDirectory(context));
        new TableWiper(databaseAdapter).wipeTable(FileResourceTableInfo.TABLE_INFO);
    }
}