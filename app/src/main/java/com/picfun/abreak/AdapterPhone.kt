package com.picfun.abreak

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @author Secret
 * @since 2019/9/18
 */
class AdapterPhone(private val data:List<DeviceRank>) : RecyclerView.Adapter<AdapterPhone.PhoneHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneHolder {
       return PhoneHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_phone,parent,false))
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: PhoneHolder, position: Int) {
        holder.nameTv.text = data[position].name
        holder.scoreTv.text = data[position].score
        holder.memory.text = data[position].memory
        holder.phone_memory.text = data[position].phonememory
        holder.brand.text = data[position].brand
        holder.devices.text = data[position].device
        holder.buId.text = data[position].buId
    }


    inner class PhoneHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameTv:TextView = itemView.findViewById(R.id.phone_name)
        var scoreTv:TextView = itemView.findViewById(R.id.phone_score)
        var memory:TextView = itemView.findViewById(R.id.phone_memory)
        var phone_memory:TextView = itemView.findViewById(R.id.phone_phonememory)
        var brand:TextView = itemView.findViewById(R.id.phone_brand)
        var devices:TextView = itemView.findViewById(R.id.phone_devices)
        var buId:TextView = itemView.findViewById(R.id.phone_buId)
    }

}