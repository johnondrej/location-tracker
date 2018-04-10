package cz.ojohn.locationtracker.screen.main

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import cz.ojohn.locationtracker.R

/**
 * Adapter for main menu RecyclerView
 */
class MenuAdapter(private val appContext: Context,
                  private var missingPermissions: List<PermissionNotice>,
                  private var controls: List<ControlItem>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    companion object {
        const val VIEW_PERMISSION_NOTICE = 1
        const val VIEW_CONTROL = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_PERMISSION_NOTICE -> {
                val view = inflater.inflate(R.layout.item_menu_notice, parent, false)
                val imgNotice = view.findViewById<ImageView>(R.id.imgNotice)
                val txtNotice = view.findViewById<TextView>(R.id.txtNotice)
                val btnNotice = view.findViewById<Button>(R.id.btnNotice)
                return MenuViewHolder.NoticeViewHolder(view, imgNotice, txtNotice, btnNotice)
            }
            VIEW_CONTROL -> {
                val view = inflater.inflate(R.layout.item_menu_control, parent, false)
                val imgIcon = view.findViewById<ImageView>(R.id.imgControl)
                val imgStatus = view.findViewById<ImageView>(R.id.imgControlStatus)
                val txtTitle = view.findViewById<TextView>(R.id.txtControlName)
                val txtDescription = view.findViewById<TextView>(R.id.txtControlStatus)
                return MenuViewHolder.ControlViewHolder(view, imgIcon, txtTitle, txtDescription, imgStatus)
            }
            else -> throw IllegalArgumentException("Invalid view type $viewType for MenuAdapter")
        }
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val itemType = holder.itemViewType
        when (itemType) {
            VIEW_PERMISSION_NOTICE -> {
                onBindPermissionViewHolder(holder as MenuViewHolder.NoticeViewHolder, position)
            }
            VIEW_CONTROL -> {
                onBindControlViewHolder(holder as MenuViewHolder.ControlViewHolder, position)
            }
        }
    }

    override fun getItemCount(): Int = missingPermissions.size + controls.size

    override fun getItemViewType(position: Int): Int {
        return if (position < missingPermissions.size) VIEW_PERMISSION_NOTICE else VIEW_CONTROL
    }

    fun updateMissingPermissions(permissionsNotice: List<PermissionNotice>) {
        missingPermissions = permissionsNotice
        notifyDataSetChanged()
    }

    fun updateMenuControls(menuControls: List<ControlItem>) {
        controls = menuControls
    }

    private fun onBindPermissionViewHolder(holder: MenuViewHolder.NoticeViewHolder, position: Int) {
        val notice = missingPermissions[position]

        holder.apply {
            imgNotice.setImageResource(notice.iconRes)
            txtNotice.text = appContext.getString(notice.textRes)
            btnNotice.text = appContext.getString(notice.buttonTextRes)
            btnNotice.setOnClickListener { notice.onClickListener() }
        }
    }

    private fun onBindControlViewHolder(holder: MenuViewHolder.ControlViewHolder, position: Int) {
        val control = controls[position - missingPermissions.size]
        val descriptionColor = ResourcesCompat.getColor(appContext.resources, control.descriptionColorRes, null)

        holder.apply {
            itemView.setOnClickListener { control.onClickListener() }
            imgIcon.setImageResource(control.iconRes)
            imgDescription.setImageResource(control.statusIconRes)
            imgDescription.setColorFilter(descriptionColor)
            txtTitle.text = appContext.getString(control.titleRes)
            txtDescription.text = appContext.getString(control.descriptionRes)
            txtDescription.setTextColor(descriptionColor)
        }
    }

    class ControlItem(val iconRes: Int,
                      val titleRes: Int,
                      val descriptionRes: Int,
                      val descriptionColorRes: Int,
                      val statusIconRes: Int,
                      val onClickListener: () -> Unit)

    class PermissionNotice(val iconRes: Int,
                           val textRes: Int,
                           val buttonTextRes: Int,
                           val onClickListener: () -> Unit)

    sealed class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        class NoticeViewHolder(itemView: View,
                               val imgNotice: ImageView,
                               val txtNotice: TextView,
                               val btnNotice: Button) : MenuViewHolder(itemView)

        class ControlViewHolder(itemView: View,
                                val imgIcon: ImageView,
                                val txtTitle: TextView,
                                val txtDescription: TextView,
                                val imgDescription: ImageView) : MenuViewHolder(itemView)
    }
}
