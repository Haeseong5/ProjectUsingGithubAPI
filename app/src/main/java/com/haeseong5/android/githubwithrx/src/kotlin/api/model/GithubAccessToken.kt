package com.haeseong5.android.githubwithrx.src.kotlin.api.model

import com.google.gson.annotations.SerializedName

/**
 * 액세스 토큰 정보를 표현하는 클래스
 *
 * Retrofit을 통해 JSON 형태로 받은 응답 데이터를 GSON 라이브러리를 이용하여
 * 클래스 형태로 변환하며, JSON 응답에 있는 필드와 클래스에 있는 필드 이름이 일치할 경우
 * 자동으로 데이터를 매핑해 준다.
 * 민약, 필드 네임을 다르게 사용하고 싶다면 어노테이션 태그 안에 새로 이름을 지어주면 된다.
 */
class GithubAccessToken(
    //json 필드에 access_token으로 저장되어 있는 값을 클래스 내 accessToken 필드에 매핑하기 위해
//SerializedName 어노테이션을 사용.
    @SerializedName("access_token") val accessToken: String,
    val scope: String,
    @SerializedName("token_type") val tokenType: String
)