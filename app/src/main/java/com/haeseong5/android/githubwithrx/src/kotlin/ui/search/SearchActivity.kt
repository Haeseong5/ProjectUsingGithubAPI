package com.haeseong5.android.githubwithrx.src.kotlin.ui.search

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider.provideGithubApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubRepo
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.RepoSearchResponse
import com.haeseong5.android.githubwithrx.src.kotlin.extensions.plusAssign
import com.haeseong5.android.githubwithrx.src.kotlin.ui.repo.RepositoryActivity
import com.haeseong5.android.githubwithrx.src.kotlin.ui.search.SearchAdapter.ItemClickListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.jetbrains.anko.startActivity
import java.lang.IllegalStateException

/**
 * SearchView 를  사용하여 검색어를 입력받고, 검색결과를 RecyclerView 를 통해 보여준다.
 */
class SearchActivity : AppCompatActivity(), ItemClickListener {

    private lateinit var menuSearch: MenuItem
    private lateinit var searchView: SearchView
    internal val adapter by lazy {
        //apply()함수를 이용하여 객체생성과 호출을 한번에 수행
        SearchAdapter().apply{
            setItemClickListener(this@SearchActivity)
        }
    }
    internal val api by lazy {
        provideGithubApi(this)
    }
//    private var searchCall: Call<RepoSearchResponse>? = null
    internal val disposables = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        adapter.setItemClickListener(this)
        rvActivitySearchList.layoutManager = LinearLayoutManager(this)
        rvActivitySearchList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)
        searchView = (menuSearch.actionView as SearchView).apply{
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    updateTitle(query)
                    hideSoftKeyboard()
                    collapseSearchView()
                    searchRepository(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    return false
                }
            })
        }

        //with 함수를 사용하여 menuSearch 범위 내에서 작업을 수행한다.
        with(menuSearch) {
            setOnActionExpandListener(object: MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    return true
                }
                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    if ("" == searchView.query){
                        finish()
                    }
                    return true
                }
            })
            expandActionView()
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.menu_activity_search_query == item.itemId) {
            item.expandActionView()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(repository: GithubRepo) {
        // apply() 를 사용하여 객체 생성과 extra 를 추가하는 작업을 동시에 수행
//        val intent = Intent(this, RepositoryActivity::class.java).apply {
//            putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
//            putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
//        }
        //anko 라이브러리 이용
        startActivity<RepositoryActivity>(
            RepositoryActivity.KEY_USER_LOGIN to repository.owner.login,
            RepositoryActivity.KEY_REPO_NAME to repository.name)

    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()
        disposables += api.searchRepository(query)
            .flatMap {
                if(0 == it.totalCount){
                    Observable.error(IllegalStateException("no search result"))
                } else {
                    Observable.just(it.items)
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe{
                clearResults()
                hideError()
                showProgress()
            }
            .doOnTerminate{
                hideProgress()
            }
            .subscribe({items ->
                with(adapter) {
                    setItems(items)
                    notifyDataSetChanged()
                }

            }) {
                showError(it.message)
            }
    }

    //검색어 결과를 액티비티 부제목으로 표시
    private fun updateTitle(query: String) {
        //null 이 아닌 경우 작업 수행
        supportActionBar?.run {
            subtitle = query
        }
    }

    private fun hideSoftKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).run{
            hideSoftInputFromWindow(searchView.windowToken, 0)
        }
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun clearResults() {
        with(adapter){
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun showProgress() {
        pbActivitySearch.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivitySearch.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivitySearchMessage){
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        with(tvActivitySearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
//        searchCall?.run { cancel() }
        disposables.clear()
    }

}