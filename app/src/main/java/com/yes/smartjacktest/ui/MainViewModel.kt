package com.yes.smartjacktest.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yes.smartjacktest.R
import com.yes.smartjacktest.api.NaverSearchApi
import com.yes.smartjacktest.model.ImageItem
import com.yes.smartjacktest.model.NaverSearchResponse
import com.yes.smartjacktest.model.NaverSearchResponse.Companion.NAVER_SEARCH_REQ_START_DEFAULT
import com.yes.smartjacktest.model.NaverSearchResponse.Companion.NAVER_SEARCH_START_MAX
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import java.util.*

class MainViewModel(private val naverSearchApi: NaverSearchApi, application: Application) : AndroidViewModel(application) {
    val searchResult: BehaviorSubject<Optional<NaverSearchResponse>>  // 검색 결과 Subject, 초기값은 빈 값
        = BehaviorSubject.createDefault(Optional.empty())

    private val imageItemList: ArrayList<ImageItem> = arrayListOf() // 추가 검색 대응을 위해 ImageItemList만 따로 보관

    val lastSearchKeyword: BehaviorSubject<Optional<String>>    // 마지막 검색어 Subject, 초기값은 빈 값
        = BehaviorSubject.createDefault(Optional.empty())

    val nextSearchAvailable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)    // 다음 검색 가능 여부 Subject, 초기값은 false

    val message: BehaviorSubject<Optional<String>> = BehaviorSubject.create()   // 화면에 표시할 메시지 Subject

    val isLoading: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)  // 로딩 상태 Subject, 초기값은 false


    fun searchImage(clientId:String, clientSecret: String, query: String, start: Int): Disposable
        = naverSearchApi.searchImage(clientId, clientSecret, query, start)
        .doOnNext { lastSearchKeyword.onNext(Optional.of(query)) }
        .flatMap { searchResponse ->
            if (searchResponse.total == 0) {
                // 검색 결과가 없을 경우 에러를 발생시켜 에러 메시지를 표시하도록 한다
                Observable.error(IllegalStateException(getString(R.string.no_search_result)))
            } else {
                // searchResult.items를 imageItemList에 추가 후, imageItemList를 다시 searchResult.items에 넣기
                imageItemList.addAll(searchResponse.items)
                searchResponse.items = ArrayList(imageItemList)

                Observable.just(searchResponse)
            }
        }

        // 구독할 때 수행할 작업
        .doOnSubscribe {
            if (start == NAVER_SEARCH_REQ_START_DEFAULT) {
                imageItemList.clear()
                searchResult.onNext(Optional.empty())
            }
            message.onNext(Optional.empty())
            isLoading.onNext(true)
        }

        // 스트림이 종료될 때 수행할 작업
        .doOnTerminate { isLoading.onNext(false) }
        .observeOn(AndroidSchedulers.mainThread())

        // 옵서버블 구독
        .subscribe({ searchResponse ->
            searchResult.onNext(Optional.of(searchResponse))

            if (searchResponse.total <= (searchResponse.start + searchResponse.display)
                || searchResponse.start >= NAVER_SEARCH_START_MAX
            ) {
                nextSearchAvailable.onNext(false)
            } else {
                nextSearchAvailable.onNext(true)
            }


        }) {
            message.onNext(Optional.of(it.message ?: getString(R.string.unexpected_error)))
        }

    private fun getString(resourceId: Int): String {
        return getApplication<Application>().getString(resourceId)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val naverSearchApi: NaverSearchApi, private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                MainViewModel(naverSearchApi, application) as T
            } else {
                throw IllegalArgumentException()
            }
        }
    }
}