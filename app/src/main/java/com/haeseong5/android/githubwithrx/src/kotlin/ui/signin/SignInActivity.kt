package com.haeseong5.android.githubwithrx.src.kotlin.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.haeseong5.android.githubwithrx.BuildConfig
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.AuthApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubAccessToken
import com.haeseong5.android.githubwithrx.src.kotlin.data.AuthTokenProvider
import com.haeseong5.android.githubwithrx.src.kotlin.ui.main.MainActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {
    //프로퍼티 선언 부분은 수동으로 수정
    //자바에서는 클래스 생성 시점에 필드값을 별도로 초기화 할 수 있지만
    //코틀린은 허용하지 않기 때문에 lateinit으로 선언한다.
    private lateinit var btnStart: Button
    private lateinit var progress: ProgressBar
    internal lateinit var api: AuthApi
    internal lateinit var authTokenProvider: AuthTokenProvider
    private lateinit var accessTokenCall: Call<GithubAccessToken>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        btnStart = findViewById(R.id.btnActivitySignInStart)
        progress = findViewById(R.id.pbActivitySignIn)

        //Github 사용자 인증을 위한 웹페이지로 이동
        btnStart.setOnClickListener{
            /**
             *사용자 인증을 처리하는 URL 구성.
             * 형식: HTTPS:/ github.com/login/oauth/authorize?clint_id={client_id}
             */
            val authUri =
                Uri.Builder().scheme("https").authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter(
                        "client_id",
                        BuildConfig.GITHUB_CLIENT_ID
                    )
                    .build()
            //크롬 커스텀 탭으로 웹페이지 표시
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        }

        api = GithubApiProvider.provideAuthApi()
        authTokenProvider = AuthTokenProvider(this) // //사용자 토큰이 있는지 여부 확인
        //저장된 액세스 토큰이 있다면 메인 액티비티로 이동
        if (null != authTokenProvider.token) {
            launchMainActivity()
        }
    }

    //SignInActivity가 화면에 표시되고 있으면 onCreate() 대신 onNewIntent()호출
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showProgress()
        //엘비스 연산자를 사용하여 null check
        //사용자 인증 완료 후 리디렉션 주소를 가져온다.
        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        //주소에서 액세스 토큰 교환에 필요한 코드를 추출
        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exists")
        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        showProgress()
        //액세스 토큰을 요청하는 RestAPI
        accessTokenCall = api.getAccessToken(
            BuildConfig.GITHUB_CLIENT_ID,
            BuildConfig.GITHUB_CLIENT_SECRET,
            code
        )

        //Call 인터페이스를 구현하는 익명 클래스의 인스턴스 생성
        //비동기 방식으로 액세스 토큰 요청
        accessTokenCall.enqueue(object : Callback<GithubAccessToken?> {
            override fun onResponse(
                call: Call<GithubAccessToken?>,
                response: Response<GithubAccessToken?>
            ) {
                hideProgress()
                val token = response.body()
                if (response.isSuccessful && null != token) {
                    authTokenProvider.updateToken(token.accessToken)
                    launchMainActivity()
                } else {
                    showError(
                        IllegalStateException(
                            "Not successful: " + response.message()
                        )
                    )
                }
            }

            override fun onFailure(
                call: Call<GithubAccessToken?>,
                t: Throwable
            ) {
                hideProgress()
                showError(t)
            }
        })
    }

    private fun showProgress() {
        btnStart.visibility = View.GONE
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnStart.visibility = View.VISIBLE
        progress.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
        Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
    }

    private fun launchMainActivity() {
        startActivity(Intent(this@SignInActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}