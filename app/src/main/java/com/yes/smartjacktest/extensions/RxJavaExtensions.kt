package com.yes.smartjacktest.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

// CompositeDisposable의 '+=' 연산자 뒤에 Disposable 타입이 오는 경우를 오버로딩함
operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    this.add(disposable)
}