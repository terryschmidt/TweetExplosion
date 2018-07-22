package com.veoride.terryschmidt.tweetexplosion

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.twitter.sdk.android.core.models.Tweet

class TweetAdapter(private var tweets: MutableList<Tweet>?) : RecyclerView.Adapter<TweetAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tweetText: TextView = view.findViewById(R.id.tweetText)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.tweet_list_entry, parent, false)
        return ViewHolder(itemView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tweet = tweets?.get(position)
        holder.tweetText.text = tweet?.text?.toString()
    }

    // swap in new Tweet list
    fun swap(newTweets: MutableList<Tweet>?) {
        tweets = newTweets
        notifyDataSetChanged()
    }

    // add 5 more new tweets to current tweet list
    fun addNewTweets(newTweets: MutableList<Tweet>?) {
        if (newTweets != null) {
            for (tweet in newTweets) {
                if (tweet != null) {
                    tweets?.add(tweet)
                }
            }
        }
        notifyDataSetChanged()
    }

    // function to get tweet list size
    override fun getItemCount(): Int {
        return tweets?.size ?: 0
    }
}