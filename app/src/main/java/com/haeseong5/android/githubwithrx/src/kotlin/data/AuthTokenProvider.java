package com.haeseong5.android.githubwithrx.src.kotlin.data;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

/**
 * 액세스 토큰을 처리하는 부분
 */
public final class AuthTokenProvider {

    private static final String KEY_AUTH_TOKEN = "auth_token";

    private Context context;

    public AuthTokenProvider(@NonNull Context context) {
        this.context = context;
    }

    //SharedPreference에 액세스 토큰을 저장
    public void updateToken(@NonNull String token) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply();
    }

    //SharedPreference에 저장되어 있는 액세스 토큰 반환
    //저장되어 있는 액세스 토큰이 없는 경우 널값 반환
    @Nullable
    public String getToken() {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null);
    }

}
