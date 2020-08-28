package com.haeseong5.android.githubwithrx.src.kotlin.api

import com.haeseong5.android.githubwithrx.src.kotlin.api.model.GithubRepo
import com.haeseong5.android.githubwithrx.src.kotlin.api.model.RepoSearchResponse
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {
    //저장소 검색 API
    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>

    //저장소 정보 읽기 API
    @GET("repos/{owner}/{name}")
    fun getRepository(@Path("owner") ownerLogin: String?,
                      @Path("name") repoName: String): Observable<GithubRepo>
}