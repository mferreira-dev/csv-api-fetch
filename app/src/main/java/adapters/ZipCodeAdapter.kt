package adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
import models.ZipCode
import pt.mferreira.wtest.R

class ZipCodeAdapter (private val context: Context, private val zipCodes: List<ZipCode>) : RecyclerView.Adapter<ZipCodeAdapter.MyViewHolder>() {
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var currentZipCode: ZipCode? = null
        private var currentPosition: Int = 0

        init {
            itemView.setOnClickListener {}
        }

        fun setData (zipCode: ZipCode?, position: Int) {
            zipCode?.let {
                val str = "${zipCode.numCodPostal}-${zipCode.extCodPostal}"
                itemView.tvArticleTitle.text = str
                itemView.tvLocalidade.text = zipCode.nomeLocalidade
                itemView.tvDesignPostal.text = zipCode.desigPostal
            }

            currentZipCode = zipCode
            currentPosition = position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val zipCode = zipCodes[position]
        holder.setData(zipCode, position)
    }

    override fun getItemCount(): Int {
        return zipCodes.size
    }
}