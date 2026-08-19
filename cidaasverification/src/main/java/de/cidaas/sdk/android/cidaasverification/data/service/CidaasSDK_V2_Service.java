package de.cidaas.sdk.android.cidaasverification.data.service;

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

public class CidaasSDK_V2_Service {
    private static Context mcontext;


    public void setContext(Context context) {
        mcontext = context;
    }

    public ICidaasSDK_V2_Services getInstance() {

        String baseurl = CidaasHelper.baseurl;

        if (baseurl == null || baseurl.isEmpty()) {
            throw new IllegalStateException("CidaasHelper.baseurl must be set before creating service instances");
        }


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

        OkHttpClient okHttpClient = clientBuilder
                .addInterceptor(new DpopInterceptor(mcontext))
                .addInterceptor(new UserAgentInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseurl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();
        return retrofit.create(ICidaasSDK_V2_Services.class);
    }
}
