package com.yes.smartjacktest.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.yes.smartjacktest.R
import com.yes.smartjacktest.api.NaverSearchApi
import com.yes.smartjacktest.api.provideNaverSearchApi
import com.yes.smartjacktest.extensions.plusAssign
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val adapter: SearchAdapter by lazy { SearchAdapter() }
    private val naverSearchApi: NaverSearchApi by lazy { provideNaverSearchApi() }
    private var nextSearchAvailable: Boolean = false
    private var query: String? = null
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        with(rvSearchedImageList) {
            adapter = this@MainActivity.adapter
            addOnScrollListener(scrollListener)
        }
    }

    override fun onStop() {
        super.onStop()

        disposables.clear()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            query = intent.getStringExtra(SearchManager.QUERY)
            searchImage(query, NAVER_SEARCH_REQ_START_DEFAULT)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (nextSearchAvailable) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {    // scroll down
                    val nextStart = adapter.itemCount + 1
                    searchImage(query, nextStart)
                }
            } else {
                showToast(getString(R.string.no_more_search_results))
            }

        }
    }

    private fun searchImage(query: String?, start: Int) {
        if (query == null || query == "")
            return

        disposables += naverSearchApi.searchImage(
            getString(R.string.naver_api_client_id), getString(R.string.naver_api_client_secret), query, start)

            // Observable 형태로 결과를 바꿔주기 위해 flatMap을 사용
            .flatMap {
                if (it.total == 0) {
                    // 검색 결과가 없을 경우 에러를 발생시켜 에러 메시지를 표시하도록 한다
                    Observable.error(IllegalStateException(getString(R.string.no_search_result)))
                } else {
                    // 검색 결과를 그대로 다음 스트림에 전달
                    Observable.just(it)
                }
            }

            // 이후 수행되는 코드는 메인 스레드에서 실행함
            .observeOn(AndroidSchedulers.mainThread())

            // 구독할 때 수행할 작업
            .doOnSubscribe {
                if (start == NAVER_SEARCH_REQ_START_DEFAULT) {
                    clearResults()
                }

                showProgress()
                hideMessage()
            }

            // 스트림이 종료될 때 수행할 작업
            .doOnTerminate { hideProgress() }

            // 옵서버블 구독
            .subscribe({ searchResponse ->
                // API를 통해 검색 결과를 정상적으로 수신 시 처리할 작업
                // 작업 중 오류 발생 시 이 블록 호출되지 않음
                when (start == NAVER_SEARCH_REQ_START_DEFAULT) {
                    true -> {   // 최초 검색
                        nextSearchAvailable = true
                        with(adapter) {
                            setItems(searchResponse.items)
                        }
                    }
                    false -> {  // 다음 결과 검색
                        if (searchResponse.total <= (searchResponse.start + searchResponse.display)
                            || searchResponse.start >= NAVER_SEARCH_START_MAX) {
                            nextSearchAvailable = false
                        }

                        with(adapter) {
                            addItems(searchResponse.items)
                            rvSearchedImageList.scrollToPosition(itemCount-1)
                        }
                    }
                }
            }) {
                // 에러 블록
                showMessage(it.message)
            }

    }

    private fun clearResults() {
        with(adapter) {
            clearItems()
            notifyDataSetChanged()
        }
    }

    private fun showProgress() {
        pbLoading.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbLoading.visibility = View.GONE
    }

    private fun showMessage(message: String?) {
        with(tvSearchMessage) {
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }

    private fun hideMessage() {
        with(tvSearchMessage) {
            text = ""
            visibility = View.GONE
        }
    }

    private fun showToast(message: String) {
        //runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        //}
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // SearchView에 searchable configuration 연결
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }

    companion object {
        const val NAVER_SEARCH_REQ_START_DEFAULT: Int = 1
        const val NAVER_SEARCH_START_MAX: Int = 1000
    }
}