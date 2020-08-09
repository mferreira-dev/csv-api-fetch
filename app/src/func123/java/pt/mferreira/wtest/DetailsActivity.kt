package pt.mferreira.wtest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.func123.activity_details.*
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_details.ivArticleDetailsImage
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsAuthor
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsDate
import kotlinx.android.synthetic.main.activity_details.tvArticleDetailsTitle
import kotlinx.android.synthetic.main.activity_details.tvArticlesDetailsBody
import models.Article
import java.lang.Exception


class DetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

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
        tvArticleDetailsDate.text = article.publishedAt.substring(0, 10)
        tvArticlesDetailsBody.text = article.body
    }
}