package org.hisp.dhis2.android.sdk.network.http;

import org.hisp.dhis2.android.sdk.utils.APIException;

public interface ApiRequestCallback<T> {
    public void onSuccess(Response response);
    public void onFailure(APIException exception);
}