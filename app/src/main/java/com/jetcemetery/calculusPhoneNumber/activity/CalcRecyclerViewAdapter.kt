package com.jetcemetery.calculusPhoneNumber.activity

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jetcemetery.androidcalculus.R

class CalcRecyclerViewAdapter(context: Context, private val integralResult_Str: MutableList<String>) :
    RecyclerView.Adapter<CalcRecyclerViewAdapter.ViewHolder>() {

    private val inflatorObj: LayoutInflater = LayoutInflater.from(context)
    private var clickListenerObj: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflatorObj.inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = integralResult_Str[position]
        holder.myTextView.text = data
    }

    override fun getItemCount(): Int = integralResult_Str.size

    fun clearData() {
        integralResult_Str.clear()
        notifyDataSetChanged()
    }

    fun addDataList(srcData: Array<String>) {
        for (str in srcData) {
            integralResult_Str.add(str)
        }
        notifyDataSetChanged()
    }

    fun getList(): Array<String> = integralResult_Str.toTypedArray()

    fun addManyResult(manyResults: Array<String>) {
        for (str in manyResults) {
            integralResult_Str.add(str)
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val myTextView: TextView = itemView.findViewById(R.id.txtV_result)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            clickListenerObj?.let {
                Log.d(TAG, "Calling onClick")
                it.onItemClick(view, bindingAdapterPosition)
                Log.d(TAG, "Post Calling onClick")
            }
        }
    }

    fun getItem(id: Int): String = integralResult_Str[id]

    fun setClickListener(itemClickListener: ItemClickListener) {
        this.clickListenerObj = itemClickListener
    }

    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    companion object {
        private const val TAG = "CalcRecyclerViewAdapter"
    }
}
