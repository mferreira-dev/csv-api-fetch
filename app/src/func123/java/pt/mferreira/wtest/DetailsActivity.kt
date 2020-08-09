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
    }
}