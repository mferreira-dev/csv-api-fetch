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
                println("ayy lmao $currentArticle")
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

                itemView.tvArticleDate.text = article.publishedAt.substring(0, 10)
                itemView.tvArticleTitle.text = article.title
                itemView.tvArticleAuthor.text = article.author
                itemView.tvArticleDescription.text = article.summary
            }

            currentArticle = article
            currentPosition = position
        }
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