package com.example.capstone.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.capstone.R
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType
import com.google.android.material.card.MaterialCardView

class MeshMessageAdapter : ListAdapter<MeshMessage, MeshMessageAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mesh_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.msgSenderName)
        private val content: TextView = itemView.findViewById(R.id.msgContent)
        private val location: TextView = itemView.findViewById(R.id.msgLocation)
        private val hopCount: TextView = itemView.findViewById(R.id.msgHopCount)
        private val relayStatus: TextView = itemView.findViewById(R.id.msgRelayStatus)
        private val statusCard: MaterialCardView = itemView.findViewById(R.id.msgStatusCard)
        private val statusText: TextView = itemView.findViewById(R.id.msgStatusText)
        private val icon: ImageView = itemView.findViewById(R.id.msgIcon)

        fun bind(message: MeshMessage) {
            val context = itemView.context
            name.text = message.senderName ?: "Unknown Device"
            content.text = message.content
            location.text = message.location?.label ?: "Unknown Location"
            
            hopCount.text = context.getString(R.string.mesh_hops_format, message.hopCount)
            relayStatus.text = if (message.hopCount > 0) {
                context.getString(R.string.mesh_relay_relayed)
            } else {
                context.getString(R.string.mesh_relay_direct)
            }
            
            when (message.type) {
                MeshMessageType.SOS -> {
                    statusCard.setCardBackgroundColor(itemView.context.getColor(R.color.color_red_600))
                    statusText.text = "Need Help"
                    icon.setImageResource(R.drawable.ic_alert_circle)
                }
                else -> {
                    statusCard.setCardBackgroundColor(itemView.context.getColor(R.color.color_green_500))
                    statusText.text = "Safe"
                    icon.setImageResource(R.drawable.ic_device)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MeshMessage>() {
        override fun areItemsTheSame(oldItem: MeshMessage, newItem: MeshMessage): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: MeshMessage, newItem: MeshMessage): Boolean = oldItem == newItem
    }
}
