package com.haeseong5.android.githubwithrx.src.kotlin.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haeseong5.android.githubwithrx.R
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApi
import com.haeseong5.android.githubwithrx.src.kotlin.api.GithubApiProvider
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubRepo
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.RepoSearchResponse
import com.haeseong5.android.githubwithrx.src.kotlin.ui.repo.RepositoryActivity
import com.haeseong5.android.githubwithrx.src.kotlin.ui.search.SearchAdapter.ItemClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * SearchView 를  사용하여 검색어를 입력받고, 검색결과를 RecyclerView 를 통해 보여준다.
 */
class SearchActivity : AppCompatActivity(), ItemClickListener {
    private lateinit var rvList: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvMessage: TextView
    private lateinit var menuSearch: MenuItem
    private lateinit var searchView: SearchView
    internal lateinit var adapter: SearchAdapter
    internal lateinit var api: GithubApi
    private lateinit var searchCall: Call<RepoSearchResponse>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        rvList = findViewById(R.id.rvActivitySearchList)
        progress = findViewById(R.id.pbActivitySearch)
        tvMessage = findViewById(R.id.tvActivitySearchMessage)
        adapter = SearchAdapter()
        adapter.setItemClickListener(this)
        rvList.layoutManager = LinearLayoutManager(this)
        rvList.adapter = adapter
        api = GithubApiProvider.provideGithubApi(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_activity_search, menu)
        menuSearch = menu.findItem(R.id.menu_activity_search_query)
        searchView = menuSearch.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
        menuSearch.expandActionView()
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
        val intent = Intent(this, RepositoryActivity::class.java)
        intent.putExtra(RepositoryActivity.KEY_USER_LOGIN, repository.owner.login)
        intent.putExtra(RepositoryActivity.KEY_REPO_NAME, repository.name)
        startActivity(intent)
    }

    private fun searchRepository(query: String) {
        clearResults()
        hideError()
        showProgress()
        searchCall = api.searchRepository(query)
        searchCall.enqueue(object : Callback<RepoSearchResponse?> {
            override fun onResponse(
                call: Call<RepoSearchResponse?>,
                response: Response<RepoSearchResponse?>
            ) {
                hideProgress()
                val searchResult = response.body()
                if (response.isSuccessful && null != searchResult) {
                    //검색 결과를 어댑터에 반영하고 갱신
                    adapter.setItems(searchResult.items)
                    adapter.notifyDataSetChanged()
                    //검색 결과가 없을 경우 Error 메세지 표시
                    if (0 == searchResult.totalCount) {
                        showError(getString(R.string.no_search_result))
                    }
                } else {
                    showError("Not successful: " + response.message())
                }
            }

            override fun onFailure(
                call: Call<RepoSearchResponse?>,
                t: Throwable
            ) {
                hideProgress()
                showError(t.message)
            }
        })
    }

    //검색어 결과를 액티비티 부제목으로 표시
    private fun updateTitle(query: String) {
        val ab = supportActionBar
        if (null != ab) {
            ab.subtitle = query
        }
    }

    private fun hideSoftKeyboard() {
        val imm =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchView.windowToken, 0)
    }

    private fun collapseSearchView() {
        menuSearch.collapseActionView()
    }

    private fun clearResults() {
        adapter.clearItems()
        adapter.notifyDataSetChanged()
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress.visibility = View.GONE
    }

    private fun showError(message: String?) {
        tvMessage.text = message
        tvMessage.visibility = View.VISIBLE
    }

    private fun hideError() {
        tvMessage.text = ""
        tvMessage.visibility = View.GONE
    }


}