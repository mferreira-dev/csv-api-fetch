package pt.mferreira.wtest

import Constants
import android.R.attr.src
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_details.*
import models.Article
import models.Articles
import singletons.VolleySingleton
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val article = intent.getSerializableExtra(Constants.DETAILS) as Article
        Picasso.get().load(article.hero).into(ivArticleDetailsImage)
        tvArticleDetailsTitle.text = article.title
        tvArticleDetailsAuthor.text = article.author
        tvArticleDetailsDate.text = article.publishedAt
        tvArticlesDetailsBody.text = article.body
    }
}