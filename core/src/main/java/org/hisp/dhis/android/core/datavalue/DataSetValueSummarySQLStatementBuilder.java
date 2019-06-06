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

package org.hisp.dhis.android.core.datavalue;

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.ReadOnlySQLStatementBuilder;
import org.hisp.dhis.android.core.arch.db.sqlorder.internal.SQLOrderType;
import org.hisp.dhis.android.core.category.CategoryOptionComboTableInfo;
import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.common.BaseIdentifiableObjectModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetDataElementLinkTableInfo;
import org.hisp.dhis.android.core.dataset.DataSetTableInfo;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitTableInfo;
import org.hisp.dhis.android.core.period.PeriodTableInfo;

public class DataSetValueSummarySQLStatementBuilder implements ReadOnlySQLStatementBuilder {

    private static final String DATAVALUE_TABLE_ALIAS = "dv";
    private static final String PERIOD_TABLE_ALIAS = "pe";
    private static final String DATASETELEMENT_TABLE_ALIAS = "dse";
    private static final String ORGUNIT_TABLE_ALIAS = "ou";
    private static final String DATASET_TABLE_ALIAS = "ds";
    private static final String AOC_TABLE_ALIAS = "aoc";

    static final String VALUE_COUNT_ALIAS = "valueCount";
    static final String DATASET_UID_ALIAS = "dataSetUid";
    static final String DATASET_NAME_ALIAS = "dataSetDisplayName";
    static final String PERIOD_ALIAS = "period";
    static final String PERIOD_TYPE_ALIAS = "periodType";
    static final String ORGANISATION_UNIT_UID_ALIAS = "organisationUnitUid";
    static final String ORGANISATION_UNIT_NAME_ALIAS = "organisationUnitDisplayName";
    static final String ATTRIBUTE_OPTION_COMBO_UID_ALIAS = "attributeOptionComboUid";
    static final String ATTRIBUTE_OPTION_COMBO_NAME_ALIAS = "attributeOptionComboDisplayName";

    static final String DATASET_UID = DATASET_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    static final String DATASET_NAME = DATASET_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.DISPLAY_NAME;
    static final String PERIOD = DATAVALUE_TABLE_ALIAS + "." + DataValueFields.PERIOD;
    static final String PERIOD_TYPE = PERIOD_TABLE_ALIAS + "." + PeriodTableInfo.Columns.PERIOD_TYPE;
    static final String ORGANISATION_UNIT_UID = ORGUNIT_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    static final String ORGANISATION_UNIT_NAME =
            ORGUNIT_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.DISPLAY_NAME;
    static final String ATTRIBUTE_OPTION_COMBO_UID = AOC_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    static final String ATTRIBUTE_OPTION_COMBO_NAME =
            AOC_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.DISPLAY_NAME;

    static final String PERIOD_START_DATE = PERIOD_TABLE_ALIAS + "." + PeriodTableInfo.Columns.START_DATE;
    static final String PERIOD_END_DATE = PERIOD_TABLE_ALIAS + "." + PeriodTableInfo.Columns.END_DATE;


    private final String STATE = DATAVALUE_TABLE_ALIAS + "." + BaseDataModel.Columns.STATE;

    private final String SELECT_STATE_ORDERING = " MAX(CASE " +
            "WHEN " + STATE + " = '" + State.SYNCED + "' THEN 1 " +
            "WHEN " + STATE + " = '" + State.TO_DELETE + "' THEN 2 " +
            "WHEN " + STATE + " IN ('" + State.TO_POST + "','" + State.TO_UPDATE + "') THEN 3 " +
            "ELSE 4 END)";

    private final String FROM_CLAUSE =
            " FROM " + DataValueTableInfo.TABLE_INFO.name() + " as " + DATAVALUE_TABLE_ALIAS +
                    getJoinPeriod() +
                    getJoinDataSetElement() +
                    getJoinDataSet() +
                    getJoinOrganisationUnit() +
                    getJoinAttributeOptionCombo();

    private final String SELECT_CLAUSE = "SELECT " +
            DATAVALUE_TABLE_ALIAS + "." + BaseDataModel.Columns.ID + ", " +
            DATASET_UID + " AS " + DATASET_UID_ALIAS + "," +
            DATASET_NAME + " AS " + DATASET_NAME_ALIAS + "," +
            PERIOD + " AS " + PERIOD_ALIAS + "," +
            PERIOD_TYPE + " AS " + PERIOD_TYPE_ALIAS + "," +
            ORGANISATION_UNIT_UID + " AS " + ORGANISATION_UNIT_UID_ALIAS + "," +
            ORGANISATION_UNIT_NAME + " AS " + ORGANISATION_UNIT_NAME_ALIAS + "," +
            ATTRIBUTE_OPTION_COMBO_UID + " AS " + ATTRIBUTE_OPTION_COMBO_UID_ALIAS + "," +
            ATTRIBUTE_OPTION_COMBO_NAME + " AS " + ATTRIBUTE_OPTION_COMBO_NAME_ALIAS + "," +
            "COUNT(*) AS " + VALUE_COUNT_ALIAS + ", " +
            STATE + ", " +
            // Auxiliary field to order the 'state' column and to prioritize TO_POST and TO_UPDATE
            SELECT_STATE_ORDERING +
            FROM_CLAUSE;

    private final String SELECT_COUNT_CLAUSE = "SELECT count(*) " + FROM_CLAUSE;

    private final String GROUP_BY_CLAUSE = " GROUP BY " +
            DATASET_UID + "," +
            PERIOD + "," +
            ORGANISATION_UNIT_UID + "," +
            ATTRIBUTE_OPTION_COMBO_UID;

    @Override
    public String selectWhere(String whereClause) {
        return SELECT_CLAUSE + " WHERE " + whereClause + GROUP_BY_CLAUSE;
    }

    @Override
    public String selectWhere(String whereClause, int limit) {
        return selectWhere(whereClause) + " LIMIT " + limit;
    }

    @Override
    public String selectAll() {
        return  SELECT_CLAUSE + GROUP_BY_CLAUSE;
    }

    @Override
    public String count() {
        return SELECT_COUNT_CLAUSE + GROUP_BY_CLAUSE;
    }

    @Override
    public String countWhere(String whereClause) {
        return SELECT_COUNT_CLAUSE + " WHERE " + whereClause + GROUP_BY_CLAUSE;
    }

    @Override
    public String selectWhere(String whereClause, String orderByClause) {
        return selectWhere(whereClause) + " ORDER BY " + orderByClause;
    }

    @Override
    public String selectWhere(String whereClause, String orderByClause, int limit) {
        return selectWhere(whereClause, orderByClause) + " LIMIT " + limit;
    }

    @Override
    public String selectOneOrderedBy(String orderingColumName, SQLOrderType orderingType) {
        return selectWhere("1", orderingColumName + " " + orderingType, 1);
    }

    private String getJoinPeriod() {
        return " INNER JOIN " + PeriodTableInfo.TABLE_INFO.name() + " as " + PERIOD_TABLE_ALIAS +
                " ON " + PERIOD + " = " + PERIOD_TABLE_ALIAS + "." + PeriodTableInfo.Columns.PERIOD_ID;
    }

    private String getJoinDataSetElement() {
        return " INNER JOIN " + DataSetDataElementLinkTableInfo.TABLE_INFO.name() + " as " + DATASETELEMENT_TABLE_ALIAS +
                " ON " + DATAVALUE_TABLE_ALIAS + "." + DataValueFields.DATA_ELEMENT + " = " +
                DATASETELEMENT_TABLE_ALIAS + "." + DataSetDataElementLinkTableInfo.Columns.DATA_ELEMENT;
    }

    private String getJoinOrganisationUnit() {
        return " INNER JOIN " + OrganisationUnitTableInfo.TABLE_INFO.name() + " as " + ORGUNIT_TABLE_ALIAS +
                " ON " + DATAVALUE_TABLE_ALIAS + "." + DataValueTableInfo.ORGANISATION_UNIT + " = " +
                ORGUNIT_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    }

    private String getJoinDataSet() {
        return " INNER JOIN " + DataSetTableInfo.TABLE_INFO.name() + " as " + DATASET_TABLE_ALIAS +
                " ON " + DATASETELEMENT_TABLE_ALIAS + "." + DataSetDataElementLinkTableInfo.Columns.DATA_SET + " = " +
                DATASET_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    }

    private String getJoinAttributeOptionCombo() {
        return " INNER JOIN " + CategoryOptionComboTableInfo.TABLE_INFO.name() + " as " + AOC_TABLE_ALIAS +
                " ON " + DATAVALUE_TABLE_ALIAS + "." + DataValueFields.ATTRIBUTE_OPTION_COMBO + " = " +
                AOC_TABLE_ALIAS + "." + BaseIdentifiableObjectModel.Columns.UID;
    }
}
