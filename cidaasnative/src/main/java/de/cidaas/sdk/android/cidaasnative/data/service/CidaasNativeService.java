package de.cidaas.sdk.android.cidaasnative.data.service;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import de.cidaas.sdk.android.helper.general.CidaasHelper;
import de.cidaas.sdk.android.helper.crypthelper.DpopInterceptor;
import de.cidaas.sdk.android.helper.network.CertificatePinningHelper;
import de.cidaas.sdk.android.helper.network.UserAgentInterceptor;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class CidaasNativeService {
    private static Context mcontext;

    public CidaasNativeService(Context context) {
        setContext(context);
    }

    private void setContext(Context context) {
        mcontext = context;
    }

    //For Cidaas Core Service
    public ICidaasNativeService getInstance() {

        String baseurl = CidaasHelper.baseurl;

        if (baseurl == null || baseurl.isEmpty()) {
            throw new IllegalStateException("CidaasHelper.baseurl must be set before creating service instances");
        }

        OkHttpClient okHttpClient = getOKHttpClient();

        ICidaasNativeService iCidaasNativeService = getRetrofit(baseurl, okHttpClient).create(ICidaasNativeService.class);
        return iCidaasNativeService;
    }


    public Retrofit getRetrofit(String baseurl, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                // .baseUrl(DBHelper.getShared().getLoginProperties().get("DomainURL"))
                .baseUrl(baseurl)//done Get Base URL
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public OkHttpClient getOKHttpClient() {
        OkHttpClient custom = CidaasHelper.customHttpClient;
        OkHttpClient.Builder clientBuilder;
        if (custom != null) {
            clientBuilder = custom.newBuilder();
        } else {
            clientBuilder = new OkHttpClient.Builder()
                    .readTimeout(40, TimeUnit.SECONDS)
                    .connectTimeout(100, TimeUnit.SECONDS);

            CertificatePinner certificatePinner = CertificatePinningHelper.buildCertificatePinner(mcontext);
            if (certificatePinner != null) {
                clientBuilder.certificatePinner(certificatePinner);
            }
        }

        return clientBuilder
                .addInterceptor(new DpopInterceptor(mcontext))
                .addInterceptor(new UserAgentInterceptor())
                .build();
    }

}
