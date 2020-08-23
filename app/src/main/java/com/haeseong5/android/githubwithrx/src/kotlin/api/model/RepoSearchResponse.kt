package com.haeseong5.android.githubwithrx.src.kotlin.api.model

import com.google.gson.annotations.SerializedName

/**
 * GithubApi 의 응답 데이터 형식 정의
 */
class RepoSearchResponse(@field:SerializedName("total_count") val totalCount: Int, val items: List<GithubRepo>)