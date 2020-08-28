package com.haeseong5.android.githubwithrx.src.kotlin.ui.repo

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider.provideGithubApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubRepo
import com.haeseong5.android.githubwithrx.src.kotlin.extensions.plusAssign
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_repository.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * 액티비티 호출 시 전달받은 사용자 이름과 저장소 이름을 사용하여 REST API 호출
 * 이를 통해 저장소 정보를 화면에 출력
 */
class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    internal val api by lazy {
        provideGithubApi(this)
    }
//    internal var repoCall: Call<GithubRepo>? = null
    internal val disposables = CompositeDisposable()
    //Rest API 응답에 포함된 날짜 및 시간 표시 형식
    internal val dateFormatInResponse = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()
    )
    //화면에서 사용자에게 보여줄 날자 및 시간 표시 형식
    val dateFormatToShow = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)


        // 액티비티 호출 시 전달받은 사용자 이름과 저장소 이름 추출
        val login =
            intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo =
            intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")
        showRepositoryInfo(login, repo)
    }

    private fun showRepositoryInfo(login: String, repoName: String) {

        showProgress()
        disposables += api.getRepository(login, repoName)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe{showProgress()}
            .doOnError{hideProgress(true)}
            .doOnComplete{hideProgress(true)}
            .subscribe({repo ->
            hideProgress(true)
                //저장소 소유자의 프로필 사진 표시
                Glide.with(this@RepositoryActivity)
                    .load(repo.owner.avatarUrl)
                    .into(ivActivityRepositoryProfile)
                //저장소 표시
                tvActivityRepositoryName.text = repo.fullName
                tvActivityRepositoryStars.text = resources
                    .getQuantityString(R.plurals.star, repo.stars, repo.stars)
                if (null == repo.description) {
                    tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                } else {
                    tvActivityRepositoryDescription.text = repo.description
                }
                if (null == repo.language) {
                    tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                } else {
                    tvActivityRepositoryLanguage.text = repo.language
                }
                try {
                    //응답에 포함된 마지막 업데이트 시간을 Date 형식으로 변환하여 표시
                    val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                    tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                } catch (e: ParseException) {
                    tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                }

            }){
                showError(it.message)
            }
    }


    private fun showProgress() {
        llActivityRepositoryContent.visibility = View.GONE
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        llActivityRepositoryContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
        pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String?) {
        //message가 null 인 경우 에러메세지 표시
        with(tvActivityRepositoryMessage) {
            text = message ?: "Unexpected Error"
            visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        disposables.clear()
    }
}