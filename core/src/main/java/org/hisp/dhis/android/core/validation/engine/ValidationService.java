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

package org.hisp.dhis.android.core.validation.engine;

import org.hisp.dhis.android.core.constant.Constant;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.parser.service.ExpressionService;
import org.hisp.dhis.android.core.parser.service.dataobject.DimensionalItemObject;
import org.hisp.dhis.android.core.validation.MissingValueStrategy;
import org.hisp.dhis.android.core.validation.ValidationRule;
import org.hisp.dhis.android.core.validation.ValidationRuleOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ValidationService {

    private ExpressionService expressionService;

    ValidationService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }

    public List<String> validate(String dataSetUid, String attributeOptionComboUid,
                                 String orgUnitUid, String periodId) {
        List<ValidationRule> rules = getValidationRulesByDataSet(dataSetUid);

        if (rules.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Constant> constantMap = getConstantMap();
        Map<DimensionalItemObject, Double> valueMap = getValueMap(dataSetUid, attributeOptionComboUid,
                orgUnitUid, periodId);

        List<String> errors = new ArrayList<>();

        for (ValidationRule rule : rules) {

        }

        return errors;
    }

    private List<String> evaluateRule(ValidationRule rule,
                                      Map<DimensionalItemObject, Double> valueMap,
                                      Map<String, Constant> constantMap,
                                      Map<String, Integer> orgunitGroupMap,
                                      Integer days) {

        // TODO Check orgunit levels

        Double leftSide = (Double) expressionService.getExpressionValue(rule.leftSide().expression(), valueMap,
                constantMap, orgunitGroupMap, days, rule.leftSide().missingValueStrategy());
        Double rightSide = (Double) expressionService.getExpressionValue(rule.rightSide().expression(), valueMap,
                constantMap, orgunitGroupMap, days, rule.rightSide().missingValueStrategy());

        // TODO
        return Collections.emptyList();
    }

    private boolean isViolation(ValidationRule rule, Double leftSide, Double rightSide) {
        if (ValidationRuleOperator.compulsory_pair.equals(rule.operator())) {
            return (leftSide == null) != (rightSide == null);
        }

        if (ValidationRuleOperator.exclusive_pair.equals(rule.operator())) {
            return (leftSide != null) && (rightSide != null);
        }

        if (leftSide == null) {
            if (rule.leftSide().missingValueStrategy() == MissingValueStrategy.NEVER_SKIP) {
                leftSide = 0d;
            } else {
                return false;
            }
        }

        if (rightSide == null) {
            if (rule.rightSide().missingValueStrategy() == MissingValueStrategy.NEVER_SKIP) {
                rightSide = 0d;
            } else {
                return false;
            }
        }

        String test = leftSide
                + rule.operator().getMathematicalOperator()
                + rightSide;
        return ! (Boolean) expressionService.getExpressionValue(test);
    }

    private List<ValidationRule> getValidationRulesByDataSet(String dataSetUid) {
        // TODO
        return Collections.emptyList();
    }

    private Map<DimensionalItemObject, Double> getValueMap(String dataSetUid, String attributeOptionComboUid,
                                                           String orgUnitUid, String periodId) {
        // TODO
        return Collections.emptyMap();
    }

    private Map<String, Constant> getConstantMap() {
        // TODO
        return Collections.emptyMap();
    }
}