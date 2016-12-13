/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.android.models.organisationunit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.models.common.BaseNameableObject;
import org.hisp.dhis.android.models.common.Field;
import org.hisp.dhis.android.models.program.Program;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

// TODO: Tests
@AutoValue
@JsonDeserialize(builder = AutoValue_OrganisationUnit.Builder.class)
public abstract class OrganisationUnit extends BaseNameableObject {
    private static final String JSON_PROPERTY_PARENT = "parent";
    private static final String JSON_PROPERTY_PATH = "path";
    private static final String JSON_PROPERTY_OPENING_DATE = "openingDate";
    private static final String JSON_PROPERTY_CLOSED_DATE = "closedDate";
    private static final String JSON_PROPERTY_LEVEL = "level";
    private static final String JSON_PROPERTY_PROGRAMS = "programs";

    public static final Field<OrganisationUnit, String> UID = Field.create(JSON_PROPERTY_UID);
    public static final Field<OrganisationUnit, String> CODE = Field.create(JSON_PROPERTY_CODE);
    public static final Field<OrganisationUnit, String> NAME = Field.create(JSON_PROPERTY_NAME);
    public static final Field<OrganisationUnit, String> DISPLAY_NAME = Field.create(JSON_PROPERTY_DISPLAY_NAME);
    public static final Field<OrganisationUnit, String> CREATED = Field.create(JSON_PROPERTY_CREATED);
    public static final Field<OrganisationUnit, String> LAST_UPDATED = Field.create(JSON_PROPERTY_LAST_UPDATED);
    public static final Field<OrganisationUnit, String> SHORT_NAME = Field.create(JSON_PROPERTY_SHORT_NAME);
    public static final Field<OrganisationUnit, String> DISPLAY_SHORT_NAME = Field.create(JSON_PROPERTY_DISPLAY_SHORT_NAME);
    public static final Field<OrganisationUnit, String> DESCRIPTION = Field.create(JSON_PROPERTY_DESCRIPTION);
    public static final Field<OrganisationUnit, String> DISPLAY_DESCRIPTION = Field.create(JSON_PROPERTY_DISPLAY_DESCRIPTION);
    public static final Field<OrganisationUnit, String> PATH = Field.create(JSON_PROPERTY_PATH);
    public static final Field<OrganisationUnit, String> OPENING_DATE = Field.create(JSON_PROPERTY_OPENING_DATE);
    public static final Field<OrganisationUnit, String> CLOSED_DATE = Field.create(JSON_PROPERTY_CLOSED_DATE);
    public static final Field<OrganisationUnit, String> LEVEL = Field.create(JSON_PROPERTY_LEVEL);
    public static final Field<OrganisationUnit, ?> PARENT = Field.create(JSON_PROPERTY_PARENT);
    public static final Field<Program, String> PROGRAMS = Field.create(JSON_PROPERTY_PROGRAMS);

    @Nullable
    @JsonProperty(JSON_PROPERTY_PARENT)
    public abstract OrganisationUnit parent();

    @Nullable
    @JsonProperty(JSON_PROPERTY_PATH)
    public abstract String path();

    @Nullable
    @JsonProperty(JSON_PROPERTY_OPENING_DATE)
    public abstract Date openingDate();

    @Nullable
    @JsonProperty(JSON_PROPERTY_CLOSED_DATE)
    public abstract Date closedDate();

    @Nullable
    @JsonProperty(JSON_PROPERTY_LEVEL)
    public abstract Integer level();

    @Nullable
    @JsonProperty(JSON_PROPERTY_PROGRAMS)
    public abstract List<Program> programs();

    public static Builder builder() {
        return new AutoValue_OrganisationUnit.Builder();
    }

    @AutoValue.Builder
    public static abstract class Builder extends BaseNameableObject.Builder<Builder> {
        @JsonProperty(JSON_PROPERTY_PARENT)
        public abstract Builder parent(@Nullable OrganisationUnit parent);

        @JsonProperty(JSON_PROPERTY_PATH)
        public abstract Builder path(@Nullable String path);

        @JsonProperty(JSON_PROPERTY_OPENING_DATE)
        public abstract Builder openingDate(@Nullable Date openingDate);

        @JsonProperty(JSON_PROPERTY_CLOSED_DATE)
        public abstract Builder closedDate(@Nullable Date closedDate);

        @JsonProperty(JSON_PROPERTY_LEVEL)
        public abstract Builder level(@Nullable Integer level);

        @JsonProperty(JSON_PROPERTY_PROGRAMS)
        public abstract Builder programs(@Nullable List<Program> programs);

        abstract List<Program> programs();

        abstract OrganisationUnit autoBuild();

        public OrganisationUnit build() {
            if (programs() != null) {
                programs(Collections.unmodifiableList(programs()));
            }

            return autoBuild();
        }
    }
}
