package cz.ojohn.locationtracker.screen.help

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cz.ojohn.locationtracker.R

/**
 * Adapter for RecyclerView with help entries
 */
class HelpAdapter(private val helpItems: Array<HelpItem>) : RecyclerView.Adapter<HelpAdapter.HelpViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_help, parent, false)
        return HelpViewHolder(view)
    }

    override fun getItemCount(): Int = helpItems.size

    override fun onBindViewHolder(holder: HelpViewHolder, position: Int) {
        holder.bind(helpItems[position])
    }

    class HelpViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

        val txtCaption = view.findViewById<TextView>(R.id.txtCaption)
        val txtHelp = view.findViewById<TextView>(R.id.txtHelp)

        fun bind(helpItem: HelpItem) {
            if (helpItem.captionRes != null) {
                txtCaption.visibility = View.VISIBLE
                txtCaption.text = view.context.getString(helpItem.captionRes)
            } else {
                txtCaption.visibility = View.GONE
            }
            txtHelp.text = view.context.getString(helpItem.helpRes)
        }
    }
}
