package com.haeseong5.android.githubwithrx.src.kotlin.api;


import android.content.Context;

import androidx.annotation.NonNull;

import com.haeseong5.android.githubwithrx.src.kotlin.data.AuthTokenProvider;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * REST API 를 실제로 호출할 수 있는 객체를 만들어주는 클래스
 * Retrofit 을 통해 REST API 를 호출하기 위해서는
 * - 호스트 서버 주소
 * - 네트워크 통신에 사용할 클라이언트 구현
 * - REST API 응답을 변환할 컨버터
 * - REST API 가 정의된 인터페이스
 * 가 필요하다.
 */
public final class GithubApiProvider {

    //액세스 토큰 획들을 위한 객체 생성 메서드
    public static AuthApi provideAuthApi() {
        return new Retrofit.Builder()
                .baseUrl("https://github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addConverterFactory(GsonConverterFactory.create()) // JSON 응답 데이터 변환
                .build()
                .create(AuthApi.class);
    }

    //저장소 정보에 접근하기 위한 객체 생성 메서드
    public static GithubApi provideGithubApi(@NonNull Context context) {
        return new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi.class);
    }

    //네트워크 통신에 사용할 클라이언트 객체 생성 메서드
    private static OkHttpClient provideOkHttpClient(
            @NonNull HttpLoggingInterceptor interceptor,
            @Nullable AuthInterceptor authInterceptor) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        if (null != authInterceptor) {
            //매 요쳥의 헤더에 액세스 토큰 정보를 추가
            b.addInterceptor(authInterceptor);
        }
        b.addInterceptor(interceptor);
        return b.build();
    }

    private static HttpLoggingInterceptor provideLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    private static AuthInterceptor provideAuthInterceptor(@NonNull AuthTokenProvider provider) {
        String token = provider.getToken();
        if (null == token) {
            throw new IllegalStateException("authToken cannot be null.");
        }
        return new AuthInterceptor(token);
    }

    private static AuthTokenProvider provideAuthTokenProvider(@NonNull Context context) {
        return new AuthTokenProvider(context.getApplicationContext());
    }

    /**
     * 저장소 정보에 접근하는 GithubAPI 는 사용자 인증정보를 헤더로 전잘해야 하므로
     * 매 요청에 인증 헤더를 추가해야 한다.
     */
    static class AuthInterceptor implements Interceptor {

        private final String token;

        AuthInterceptor(String token) {
            this.token = token;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            //요청의 헤더에 엑세스 토큰 정보를 추가
            Request.Builder b = original.newBuilder()
                    .addHeader("Authorization", "token " + token);

            Request request = b.build();
            return chain.proceed(request);
        }
    }
}
