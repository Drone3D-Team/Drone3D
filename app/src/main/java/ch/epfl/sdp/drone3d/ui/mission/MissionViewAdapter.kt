/*
 * Copyright (C) 2021  Drone3D-Team
 * The license can be found in LICENSE at root of the repository
 */

package ch.epfl.sdp.drone3d.ui.mission

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ch.epfl.sdp.drone3d.R
import ch.epfl.sdp.drone3d.service.storage.data.MappingMission
import ch.epfl.sdp.drone3d.service.storage.data.State

/**
 * Adapter for the Recycler view creating holders for the missions
 */
class MissionViewAdapter(private val privateList: Boolean) :
        ListAdapter<MappingMission, MissionViewAdapter.MissionViewHolder>(MissionDiff) {

    class MissionViewHolder(view: View, private val privateList: Boolean) : RecyclerView.ViewHolder(view) {

        private val textView: TextView = view.findViewById(R.id.mapping_selection_item_button)
        private var curMission: MappingMission? = null

        init {
            textView.setOnClickListener {
                curMission?.let {
                    //TODO: go to mission details
                }
            }
        }

        // Bind holder with a mission -> set the name
        fun bind(mission: MappingMission) {
            curMission = mission
            textView.text =
                if (mission.state == State.PRIVATE_AND_SHARED)
                    if (privateList)
                        itemView.context.getString(R.string.mapping_mission_list_format_private, mission.name)
                    else
                        itemView.context.getString(R.string.mapping_mission_list_format_shared, mission.name)
                else
                    itemView.context.getString(R.string.mapping_mission_list_format, mission.name)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.mission_selection_item, parent, false)

        return MissionViewHolder(view, privateList)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object MissionDiff : DiffUtil.ItemCallback<MappingMission>() {

        override fun areItemsTheSame(oldItem: MappingMission, newItem: MappingMission): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: MappingMission, newItem: MappingMission): Boolean {
            return oldItem.name == newItem.name && oldItem.state == newItem.state
        }
    }
}

