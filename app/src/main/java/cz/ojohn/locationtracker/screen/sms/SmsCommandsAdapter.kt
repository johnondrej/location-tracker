package cz.ojohn.locationtracker.screen.sms

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cz.ojohn.locationtracker.R
import cz.ojohn.locationtracker.sms.SmsController

/**
 * Adapter for RecyclerView with commands list
 */
class SmsCommandsAdapter(private val smsCommands: Array<SmsCommand>,
                         private val onCommandSelectedListener: (String) -> Unit) : RecyclerView.Adapter<SmsCommandsAdapter.CommandViewHolder>() {

    companion object {
        fun formatCommand(context: Context, command: SmsCommand): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append(SmsController.SMS_KEYWORD)
                    .append(' ')
            if (command.requiresPassword) {
                stringBuilder.append(context.getString(R.string.sms_description_password))
                        .append(' ')
            }
            stringBuilder.append(command.command)
            return stringBuilder.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommandViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sms_command, parent, false)
        return CommandViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommandViewHolder, position: Int) {
        val command = smsCommands[position]
        holder.bind(command, { onCommandSelectedListener(formatCommand(holder.itemView.context, command)) })
    }

    override fun getItemCount(): Int = smsCommands.size

    class CommandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val txtName: TextView = itemView.findViewById(R.id.txtCommandName)
        private val txtDesc: TextView = itemView.findViewById(R.id.txtCommandDescription)

        fun bind(smsCommand: SmsCommand, onSelectedListener: (SmsCommand) -> Unit) {
            itemView.setOnClickListener { onSelectedListener(smsCommand) }
            txtName.text = formatCommand(itemView.context, smsCommand)
            txtDesc.text = itemView.context.getString(smsCommand.descriptionRes)
        }
    }
}
