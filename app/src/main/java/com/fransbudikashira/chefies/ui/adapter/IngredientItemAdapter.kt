package com.fransbudikashira.chefies.ui.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fransbudikashira.chefies.R
import com.fransbudikashira.chefies.databinding.ItemIngredientBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class IngredientItemAdapter(
    private val updateCallback: (Int, String) -> Unit,
    private val deleteCallback: (Int) -> Unit,
): ListAdapter<String, IngredientItemAdapter.MyViewHolder>(DIFF_CALLBACK) {
    class MyViewHolder(
        private val binding: ItemIngredientBinding,
        private val updateCallback: (Int, String) -> Unit,
        private val deleteCallback: (Int) -> Unit,
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(resultItem: String, position: Int) {
            binding.apply {
                title.text = resultItem
                actionButton(position, resultItem)
            }
        }
        private fun actionButton(position: Int, resultItem: String) {
            binding.apply {
                //update button
                btnEdit.setOnClickListener {
                    editDialog(itemView.context, position, resultItem)
                }
                //delete button
                btnDelete.setOnClickListener {
                    deleteDialog(itemView.context, position)
                }
            }
        }

        private fun editDialog(context: Context, position: Int, resultItem: String) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog_edit_ingredient)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0.5f)

            // Handle Input Edit Text
            val input: TextInputEditText = dialog.findViewById(R.id.edtEdit)
            input.setText(resultItem)

            // Request focus and show keyboard
            input.requestFocus()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }, 300)

            val btnUpdate: MaterialButton = dialog.findViewById(R.id.btnUpdateIngredient)
            btnUpdate.setOnClickListener {
                // Exception Input
                val inputLayout: TextInputLayout = dialog.findViewById(R.id.edtEditLayout)
                val inputText: String = input.text?.trim().toString()
                if (inputText.length < 3){
                    inputLayout.error = context.getString(R.string.invalid_min3_characters)
                    return@setOnClickListener
                } else {
                    inputLayout.error = null
                    dialog.dismiss()
                }

                // Main Action
                updateCallback(position, inputText)
            }

            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
        }

        private fun deleteDialog(context: Context, position: Int) {
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.custom_dialog_save_result)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setDimAmount(0.5f)

            // Change Title & Message Content
            val title: TextView = dialog.findViewById(R.id.title)
            val message: TextView = dialog.findViewById(R.id.message)
            title.text = context.getString(R.string.delete_title)
            title.setTextColor(context.getColor(R.color.red))
            message.text = context.getString(R.string.delete_message)

            val btnYes: MaterialButton = dialog.findViewById(R.id.btn_yes)
            val btnNo: MaterialButton = dialog.findViewById(R.id.btn_no)
            btnYes.setOnClickListener {
                dialog.dismiss()
                // Main Action
                deleteCallback(position)
            }
            btnNo.setOnClickListener { dialog.dismiss() }

            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val binding = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding, updateCallback ,deleteCallback)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result, position)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}