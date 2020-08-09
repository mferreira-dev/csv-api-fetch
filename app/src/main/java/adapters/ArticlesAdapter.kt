package adapters

import Constants
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.article_item.view.*
import kotlinx.android.synthetic.main.list_item.view.tvArticleTitle
import models.Article
import pt.mferreira.wtest.DetailsActivity
import pt.mferreira.wtest.R

class ArticlesAdapter (private val context: Context, private val articles: List<Article>) : RecyclerView.Adapter<ArticlesAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentArticle: Article? = null
        private var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {
                println("ayy lmao $currentArticle")
                val intent = Intent(context, DetailsActivity::class.java)
                intent.putExtra(Constants.DETAILS, currentArticle)
                context.startActivity(intent)
            }
        }

        fun setData (article: Article?, position: Int) {
            article?.let {
                if (article.hero != null) Picasso.get().load(article.hero).into(itemView.tvArticleImage)
                else {
                    val params: FrameLayout.LayoutParams = itemView.tvArticleImage.layoutParams as FrameLayout.LayoutParams
                    params.height = context.resources.getDimensionPixelSize(R.dimen.item_view_height);
                    itemView.tvArticleImage.layoutParams = params
                }

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