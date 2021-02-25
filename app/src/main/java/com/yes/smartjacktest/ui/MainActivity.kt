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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.yes.smartjacktest.BuildConfig
import com.yes.smartjacktest.R
import com.yes.smartjacktest.api.NaverSearchApi
import com.yes.smartjacktest.api.provideNaverSearchApi
import com.yes.smartjacktest.extensions.plusAssign
import com.yes.smartjacktest.model.NaverSearchResponse.Companion.NAVER_SEARCH_REQ_START_DEFAULT
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val searchAdapter: SearchAdapter by lazy { SearchAdapter() }
    private var nextSearchAvailable: Boolean = false
    private var lastKeyword: String? = null
    private val disposables = CompositeDisposable()
    private val viewModelFactory by lazy {
        MainViewModel.Factory(
            provideNaverSearchApi(),
            application
        )
    }
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        with(rvSearchedImageList) {
            adapter = this@MainActivity.searchAdapter
            addOnScrollListener(scrollListener)
        }

        disposables += viewModel.searchResult
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { optSearchResponse ->
                with(searchAdapter) {
                    if (optSearchResponse.isPresent) {
                        val searchRes = optSearchResponse.get()

                        if (itemCount == 0) {   // 최초 검색
                            setItems(searchRes.items)
                        } else {    // 이어서 검색
                            val startIdx = searchRes.start - 1
                            val endIdx = startIdx + searchRes.display
                            addItems(searchRes.items.subList(startIdx, endIdx))
                        }

                        rvSearchedImageList.scrollToPosition(itemCount-1)
                    } else {
                        clearItems()
                    }
                }
            }

        disposables += viewModel.message
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { optMessage ->
                if (optMessage.isPresent) {
                    showMessage(optMessage.get())
                } else {
                    hideMessage()
                }
            }

        disposables += viewModel.isLoading
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { isLoading ->
                if (isLoading) {
                    showProgress()
                } else {
                    hideProgress()
                }
            }

        disposables += viewModel.nextSearchAvailable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                this.nextSearchAvailable = it
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
            searchImage(intent.getStringExtra(SearchManager.QUERY), NAVER_SEARCH_REQ_START_DEFAULT)
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {    // scroll down
                if (searchAdapter.itemCount == 0)   // 아직 검색하지 않은 경우 예외처리
                    return

                if (nextSearchAvailable) {
                    val nextStart = searchAdapter.itemCount + 1
                    searchImage(lastKeyword, nextStart)
                } else {
                    showToast(getString(R.string.no_more_search_results))
                }
            }
        }
    }

    private fun searchImage(query: String?, start: Int) {
        if (query == null || query == "")
            return

        disposables += viewModel.searchImage(
            BuildConfig.NAVER_API_CLIENT_ID,
            BuildConfig.NAVER_API_CLIENT_SECRET,
            query,
            start)
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
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // SearchView에 searchable configuration 연결
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            setIconifiedByDefault(true)

            disposables += viewModel.lastSearchKeyword
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { optLastKeyword ->
                    if (optLastKeyword.isPresent) {
                        lastKeyword = optLastKeyword.get()
                    }
                }
        }

        return true
    }
}