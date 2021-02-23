package com.yes.smartjacktest

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.yes.smartjacktest.api.NaverSearchApi
import com.yes.smartjacktest.api.provideNaverSearchApi
import com.yes.smartjacktest.model.NaverSearchResponse
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {

    private val adapter: SearchAdapter by lazy { SearchAdapter() }
    private val naverSearchApi: NaverSearchApi by lazy { provideNaverSearchApi() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvSearchedImageList.adapter = this.adapter
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (query != null) {
                searchImage(query)
            }
        }
    }

    private fun searchImage(query: String) {
        clearResults()
        showProgress()
        hideMessage()

        val searchCall = naverSearchApi.searchImage(getString(R.string.naver_api_client_id),
            getString(R.string.naver_api_client_secret),
            query)
        searchCall.enqueue(object : Callback<NaverSearchResponse> {
            override fun onResponse(
                call: Call<NaverSearchResponse>,
                response: Response<NaverSearchResponse>) {
                hideProgress()

                val searchResult = response.body()
                if (response.isSuccessful && searchResult != null) {
                    if (searchResult.total == 0) {
                        showMessage(getString(R.string.no_search_result))
                    } else {
                        with(adapter) {
                            setItems(searchResult.items)
                            notifyDataSetChanged()
                        }
                    }
                } else {
                    showMessage("Search failed: " + response.message())
                }
            }

            override fun onFailure(call: Call<NaverSearchResponse>, t: Throwable) {
                hideProgress()
                showMessage(t.message)
            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        // SearchView에 searchable configuration 연결
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }
}