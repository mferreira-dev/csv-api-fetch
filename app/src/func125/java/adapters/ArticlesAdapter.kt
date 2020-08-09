package adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.func125.article_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvArticleTitle
import models.Article
import pt.mferreira.wtest.DetailsActivity
import pt.mferreira.wtest.R
import java.lang.Exception

class ArticlesAdapter (private val context: Context, private val articles: List<Article>) : RecyclerView.Adapter<ArticlesAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentArticle: Article? = null
        private var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra("details", currentArticle)
                context.startActivity(intent)
            }
        }

        fun setData (article: Article?, position: Int) {
            article?.let {
                if (article.hero != null) {
                    itemView.tvArticleImage.setScaleType(ImageView.ScaleType.CENTER_CROP)

                    Picasso.get().load(article.hero).into(itemView.tvArticleImage, object : Callback {
                        override fun onSuccess() {
                            itemView.articleProgressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {

                        }
                    })
                }
                else {
                    val params: LinearLayout.LayoutParams = itemView.articleCardView.layoutParams as LinearLayout.LayoutParams
                    params.height = context.resources.getDimensionPixelSize(R.dimen.item_view_height);
                    itemView.articleCardView.layoutParams = params
                    itemView.articleProgressBar.visibility = View.GONE
                }

                itemView.tvArticleDate.text = formatDate(article.publishedAt.substring(0, 10))

                if (article.title.length > 15) itemView.tvArticleTitle.text = "${article.title.substring(0, 16)}..."
                else itemView.tvArticleTitle.text = article.title

                itemView.tvArticleAuthor.text = article.author
                itemView.tvArticleDescription.text = article.summary
            }

            currentArticle = article
            currentPosition = position
        }
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val article = articles[position]
        holder.setData(article, position)
    }

    override fun getItemCount(): Int {
        return articles.size
    }
}