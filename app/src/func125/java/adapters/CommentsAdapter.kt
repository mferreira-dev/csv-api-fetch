package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.func125.comment_item.view.*
import models.Article
import models.Comment
import pt.mferreira.wtest.R
import java.lang.Exception

class CommentsAdapter (private val context: Context, private val comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentComment: Comment? = null
        private var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {}
        }

        fun setData (comment: Comment?, position: Int) {
            comment?.let {
                if (comment.avatar != null) {
                    itemView.ivCommentAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP)

                    Picasso.get().load(comment.avatar).into(itemView.ivCommentAvatar, object : Callback {
                        override fun onSuccess() {
                            itemView.commentAvatarProgressBar.visibility = View.GONE
                        }

                        override fun onError(e: Exception?) {

                        }
                    })
                }
                else {
                    val params: LinearLayout.LayoutParams = itemView.commentCardView.layoutParams as LinearLayout.LayoutParams
                    params.height = context.resources.getDimensionPixelSize(R.dimen.item_view_height);
                    itemView.commentCardView.layoutParams = params
                    itemView.commentAvatarProgressBar.visibility = View.GONE
                }

                itemView.tvCommentAuthor.text = comment.name
                itemView.tvCommentDate.text = comment.publishedAt.subSequence(0, 10)
                itemView.tvCommentComment.text = comment.body
            }

            currentComment = comment
            currentPosition = position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.comment_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val comment = comments[position]
        holder.setData(comment, position)
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}