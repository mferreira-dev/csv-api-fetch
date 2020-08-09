package pt.mferreira.wtest

import adapters.ArticlesAdapter
import adapters.CommentsAdapter
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
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.func125.activity_details.*
import kotlinx.android.synthetic.main.activity_articles.*
import kotlinx.android.synthetic.main.activity_details.ivArticleDetailsImage
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsAuthor
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsDate
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsTitle
import kotlinx.android.synthetic.main.activity_details.tvArticlesDetailsBody
import models.Article
import models.Comment
import singletons.VolleySingleton
import java.lang.Exception


class DetailsActivity : AppCompatActivity() {
    private var comments: MutableList<Comment> = ArrayList()
    private val adapter = CommentsAdapter(this, comments)
    lateinit var context: Context
    var page = 1

    inner class DownloadComments : AsyncTask<String, Void, Int>() {
        override fun doInBackground(vararg params: String): Int {
            if (params.isNotEmpty()) {
                val jor = JsonArrayRequest(Request.Method.GET, params[0], null, Response.Listener {
                    // Nameless json array.
                    val resp = Gson().fromJson(it.toString(), Array<Comment>::class.java)
                    for (i in resp.indices) comments.add(resp[i])

                    adapter.notifyDataSetChanged()
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
        commentsRecyclerView.layoutManager = layoutManager

        // Load more entries when the user scrolls all the way to the bottom of the RecyclerView.
        commentsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {

                }
            }
        })

        commentsRecyclerView.adapter = adapter
    }

    private fun formatDate(s: String): String {
        var str = ""

        val split = s.substring(0, 10).split("-")

        when (split[1]) {
            "01" -> { str = "Jan." }
            "02" -> { str = "Feb." }
            "03" -> { str = "Mar." }
            "04" -> { str = "Apr." }
            "05" -> { str = "May" }
            "06" -> { str = "Jun." }
            "07" -> { str = "Jul." }
            "08" -> { str = "Aug." }
            "09" -> { str = "Sep."}
            "10" -> { str = "Oct." }
            "11" -> { str = "Nov." }
            "12" -> { str = "Dec." }
        }

        val day = split[2]
        var ins = when {
            day[day.length - 1] == '1' -> "${day}st"
            day[day.length - 1] == '2' -> "${day}nd"
            day[day.length - 1] == '3' -> "${day}rd"
            else -> { "${day}th" }
        }

        if (ins[0] == '0') ins = ins.replace("0", "")
        str = "$str $ins, ${split[0].substring(0, 4)}"

        return str
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        context = this
        setupRecyclerView()

        val article = intent.getSerializableExtra("details") as Article
        Picasso.get().load(article.hero).into(ivArticleDetailsImage, object : Callback {
            override fun onSuccess() {
                articleDetailsProgressBarHeader.visibility = View.GONE
            }

            override fun onError(e: Exception?) {

            }
        })

        tvArticleDetailsTitle.text = article.title
        tvArticleDetailsAuthor.text = article.author
        tvArticleDetailsDate.text = formatDate(article.publishedAt.substring(0, 10))
        tvArticlesDetailsBody.text = article.body

        Picasso.get().load(article.avatar).into(ivArticleAuthorAvatar, object : Callback {
            override fun onSuccess() {
                articleDetailsProgressBarAvatar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {

            }
        })

        DownloadComments().execute("https://5bb1d1e66418d70014071c9c.mockapi.io/mobile/2-0/articles/${article.id}/comments")
    }
}