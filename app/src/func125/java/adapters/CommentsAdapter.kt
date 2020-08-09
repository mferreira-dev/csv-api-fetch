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
                itemView.tvCommentDate.text = formatDate(comment.publishedAt.substring(0, 10))
                itemView.tvCommentComment.text = comment.body
            }

            currentComment = comment
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