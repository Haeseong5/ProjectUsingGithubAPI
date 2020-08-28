package com.haeseong5.android.githubwithrx.src.kotlin.api

import android.content.Context
import com.haeseong5.android.githubwithrx.src.kotlin.data.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/**
 * REST API 를 실제로 호출할 수 있는 객체를 만들어주는 클래스
 * Retrofit 을 통해 REST API 를 호출하기 위해서는
 * - 호스트 서버 주소
 * - 네트워크 통신에 사용할 클라이언트 구현
 * - REST API 응답을 변환할 컨버터
 * - REST API 가 정의된 인터페이스
 * 가 필요하다.
 */
//싱글톤 클래스
object GithubApiProvider {
    //액세스 토큰 획들을 위한 객체 생성 메서드
    //단순히 생성한 객체를 반환하는 기능을 수행하기 때문에 단일표현식으로 리팩토링
    fun provideAuthApi(): AuthApi
        = Retrofit.Builder()
            .baseUrl("https://github.com/")
            .client(
                provideOkHttpClient(provideLoggingInterceptor(), null))
            //받은 응답을 옵저버블 형태로 변환하며, 비동기 방식으로 API 를 호출
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create()) // JSON 응답 데이터 변환
            .build()
            .create(AuthApi::class.java)


    //저장소 정보에 접근하기 위한 객체 생성 메서드
    fun provideGithubApi(context: Context): GithubApi
        = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(provideOkHttpClient(provideLoggingInterceptor(),
                    provideAuthInterceptor(provideAuthTokenProvider(context))))
        //받은 응답을 옵저버블 형태로 변환하며, 비동기 방식으로 API 를 호출
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GithubApi::class.java)


    //네트워크 통신에 사용할 클라이언트 객체 생성 메서드
    private fun provideOkHttpClient(
        interceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor?
    ): OkHttpClient
    = OkHttpClient.Builder()
        .run{
            if (null != authInterceptor) { //매 요쳥의 헤더에 액세스 토큰 정보를 추가
                addInterceptor(authInterceptor)
            }
            addInterceptor(interceptor)
            build()
        }


    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    private fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor {
        val token = provider.token ?: throw IllegalStateException("authToken cannot be null.")
        return AuthInterceptor(token)
    }

    private fun provideAuthTokenProvider(context: Context): AuthTokenProvider
        = AuthTokenProvider(context.applicationContext)


    /**
     * 저장소 정보에 접근하는 GithubAPI 는 사용자 인증정보를 헤더로 전달해야 하므로
     * 매 요청에 인증 헤더를 추가해야 한다.
     */
    internal class AuthInterceptor(private val token: String) : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            //요청의 헤더에 엑세스 토큰 정보를 추가
            val b = original.newBuilder()
                .addHeader("Authorization", "token $token")
            val request = b.build()
            return chain.proceed(request)
        }

    }
}