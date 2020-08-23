package com.haeseong5.android.githubwithrx.src.kotlin.api.model

import com.google.gson.annotations.SerializedName

/**
 * REST API를 통해 받는 응답을 올바르게 매핑하기 위해서는 NULL 값 처리를 적절하게 해줘야 한다.
 * 코틀린에서 특정 필드에 널값을 허용하지 않도록 설정한다 해도
 * Gson에서는 이 필드에 널 값을 할당할 수 있기 때문이다.
 * 따라서 Null 을 허용하는 프로퍼티와 그렇지 않은 프로퍼티를 잘 구분해야 한다.
 */
class GithubRepo(
    val name: String,
    @field:SerializedName("full_name") val fullName: String,
    val owner: GithubOwner,
    val description: String?, // Null 값 허용
    val language: String?, //Nullable

    @field:SerializedName("updated_at") val updatedAt: String,

    @field:SerializedName("stargazers_count") val stars: Int
)