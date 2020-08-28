package com.haeseong5.android.githubwithrx.src.kotlin.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.haeseong5.android.githubwithrx.BuildConfig
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider.provideAuthApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubAccessToken
import com.haeseong5.android.githubwithrx.src.kotlin.data.AuthTokenProvider
import com.haeseong5.android.githubwithrx.src.kotlin.extensions.plusAssign
import com.haeseong5.android.githubwithrx.src.kotlin.ui.main.MainActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.longToast

class SignInActivity : AppCompatActivity() {

    //인증이 완료된 사용자의 api를 얻기 위함
    internal val api by lazy {
        provideAuthApi()
    }
    //AuthAPI의 인스턴스를 얻기 위함
    internal val authTokenProvider by lazy {
        AuthTokenProvider(this)
    }
    // internal var accessTokenCall: Call<GithubAccessToken>? = null //null값 허용 후 명시적으로 null값 할당
    // 여러 disposable 객체를 관리할 수 있는 CompositeDisposable 객체를 초기화합니다.
    internal val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

//        Github 사용자 인증을 위한 웹페이지로 이동
        btnActivitySignInStart.setOnClickListener{
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

//        api = GithubApiProvider.provideAuthApi()
//        authTokenProvider = AuthTokenProvider(this) // //사용자 토큰이 있는지 여부 확인
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
        disposables += api.getAccessToken(BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
            .map { it.accessToken } //Rest API를 통해 받은 응답에서 액세스 토큰만 추출
            .observeOn(AndroidSchedulers.mainThread()) //메인스레드에서 실행
            .doOnSubscribe{showProgress()}//구독할 때 수행할 작업 구현
            .doOnTerminate{hideProgress()}  //스트림이 종료될 때 작업 구현
            .subscribe({token -> //옵저 버블을 구독
                //API 를 통해 액세스 토큰을 정상적으로 받았을 때 호출
                authTokenProvider.updateToken(token)
                launchMainActivity()
            }){//에러 발생 시 해당 블록 호출
                showError(it)
            }


//        //Call 인터페이스를 구현하는 익명 클래스의 인스턴스 생성
//        //비동기 방식으로 액세스 토큰 요청
//        accessTokenCall!!.enqueue(object : Callback<GithubAccessToken?> {
//            override fun onResponse(
//                call: Call<GithubAccessToken?>,
//                response: Response<GithubAccessToken?>
//            ) {
//                hideProgress()
//                val token = response.body()
//                if (response.isSuccessful && null != token) {
//                    authTokenProvider.updateToken(token.accessToken)
//                    launchMainActivity()
//                } else {
//                    showError(
//                        IllegalStateException(
//                            "Not successful: " + response.message()
//                        )
//                    )
//                }
//            }
//
//            override fun onFailure(
//                call: Call<GithubAccessToken?>,
//                t: Throwable
//            ) {
//                hideProgress()
//                showError(t)
//            }
//        })
    }

    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
//        Toast.makeText(this, throwable.message, Toast.LENGTH_LONG).show()
        longToast(throwable.message ?: "No message available") //Anko Library
    }

    private fun launchMainActivity() {
        startActivity(Intent(this@SignInActivity, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override fun onStop() {
        super.onStop()
        //액티비티가 화면에서 사라지는 시점에 api 호출 객체가 생성되어 있다면
        //api 요청을 취소
//        accessTokenCall?.run{
//            cancel()
//        }
        //관리하고 있던 디스포저블 객체 모두 해제
        disposables.clear()
    }

}