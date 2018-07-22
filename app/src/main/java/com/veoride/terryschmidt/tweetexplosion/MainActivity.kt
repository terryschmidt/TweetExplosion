package com.veoride.terryschmidt.tweetexplosion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import com.twitter.sdk.android.core.models.Search
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.core.models.User

class MainActivity : AppCompatActivity() {

    private lateinit var twitterLoginButton: TwitterLoginButton
    private lateinit var twitterSession: TwitterSession
    private lateinit var twitterAuthToken: TwitterAuthToken
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var tweetRecycler: RecyclerView
    private lateinit var tweetAdapter: TweetAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var tweets: MutableList<Tweet>? = null
    private var removedTweetCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewManager = LinearLayoutManager(this)
        tweetAdapter = TweetAdapter(tweets)
        tweetRecycler = findViewById<RecyclerView>(R.id.tweetRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = tweetAdapter
        }
        tweetRecycler.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        searchEditText = findViewById(R.id.searchEditText)
        twitterLoginButton = findViewById(R.id.twitterLoginButton)
        searchButton = findViewById(R.id.searchButton)
        createItemTouchHelper()
        setLoginButtonCallback()
    }

    private fun createItemTouchHelper() {
        val itemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, viewHolder1: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                tweets?.removeAt(position)
                tweetAdapter.notifyItemRemoved(position)
                removedTweetCount++
                if (removedTweetCount == 5) {
                    fetchTweets(5)
                    removedTweetCount = 0
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(tweetRecycler)
    }

    private fun setLoginButtonCallback() {
        twitterLoginButton.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                twitterSession = TwitterCore.getInstance().sessionManager.activeSession
                twitterAuthToken = twitterSession.authToken
                val userID = result.data.id
                loadTwitterAPI(userID)
            }

            override fun failure(exception: TwitterException) {
                Log.e(TAG, "Failed to set login button callback: " + exception.toString())
            }
        }
    }

    private fun loadTwitterAPI(userID: Long) {
        TweetExplosionApiClient(twitterSession).customService.show(userID).enqueue(object : Callback<User>() {
            override fun success(result: Result<User>) {
                twitterLoginButton.visibility = View.GONE
                searchEditText.visibility = View.VISIBLE
                searchButton.visibility = View.VISIBLE
            }

            override fun failure(exception: TwitterException) {
                Log.e(TAG, "Failed to load twitter API: " + exception.toString())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterLoginButton.onActivityResult(requestCode, resultCode, data)
    }

    fun searchButtonPressed(view: View) {
        removedTweetCount = 0
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        fetchTweets(15)
    }

    private fun fetchTweets(numberOfTweetsToFetch: Int) {
        val searchText = "#" + searchEditText.text.toString()
        TweetExplosionApiClient(twitterSession).searchService.tweets(
                searchText,
                null,
                "",
                "",
                "",
                numberOfTweetsToFetch,
                "",
                0,
                0,
                true)
                .enqueue(object: Callback<Search>() {
                    override fun success(result: Result<Search>?) {
                        Log.d(TAG, "success on thread: " + Thread.currentThread().name)
                        if (numberOfTweetsToFetch == 15) {
                            tweets = result?.data?.tweets?.toMutableList()
                            tweetRecycler.visibility = View.VISIBLE
                            tweetAdapter.swap(tweets)
                        } else if (numberOfTweetsToFetch == 5) {
                            tweetAdapter.addNewTweets(result?.data?.tweets?.toMutableList())
                        }
                    }

                    override fun failure(exception: TwitterException?) {
                        Log.e(TAG, "Failed to search tweets: " + exception?.message)
                    }
                })
    }

    companion object {
        const val TAG = "MainActivity"
    }
}