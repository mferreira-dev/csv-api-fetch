package pt.mferreira.wtest

import adapters.ArticlesAdapter
import android.content.ClipData.Item
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_articles.*
import models.Article
import singletons.VolleySingleton


class ArticlesActivity : AppCompatActivity() {
    lateinit var context: Context
    private var articles: MutableList<Article> = ArrayList()
    private val adapter = ArticlesAdapter(this, articles)
    var page = 1

    inner class DownloadFromApi : AsyncTask<String, Void, Int>() {
        override fun doInBackground(vararg params: String): Int {
            if (params.isNotEmpty()) {
                val jor = JsonArrayRequest(Request.Method.GET, params[0], null, Response.Listener {
                        // Nameless json array.
                        val resp = Gson().fromJson(it.toString(), Array<Article>::class.java)
                        for (i in resp.indices) articles.add(resp[i])

                        adapter.notifyDataSetChanged()
                        changeLoadState(false)
                        page++
                    },
                    Response.ErrorListener {
                        Toast.makeText(context, "Error while performing request. Please check your connection or try again later.", Toast.LENGTH_SHORT).show()
                    }
                )

                VolleySingleton.getInstance(context).addToRequestQueue(jor)
            }

            return 1
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        articlesRecyclerView.layoutManager = layoutManager

        // Load more entries when the user scrolls all the way to the bottom of the RecyclerView.
        articlesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    // load more stuff
                    DownloadFromApi().execute("https://5bb1d1e66418d70014071c9c.mockapi.io/mobile/2-0/articles?limit=15&page=$page")
                }
            }
        })

        articlesRecyclerView.adapter = adapter
    }

    private fun changeLoadState (state: Boolean) {
        if (state) progressArticles.visibility = View.VISIBLE
        else progressArticles.visibility = View.INVISIBLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articles)
        context = this
        changeLoadState(true)

        setupRecyclerView()
        DownloadFromApi().execute("https://5bb1d1e66418d70014071c9c.mockapi.io/mobile/2-0/articles?limit=15&page=$page")
    }
}