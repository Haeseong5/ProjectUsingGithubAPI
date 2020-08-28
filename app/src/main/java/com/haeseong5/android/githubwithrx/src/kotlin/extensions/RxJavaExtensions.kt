package com.haeseong5.android.githubwithrx.src.kotlin.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

operator fun CompositeDisposable.plusAssign(disposable: Disposable){
    this.add(disposable)
}