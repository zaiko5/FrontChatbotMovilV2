package com.example.frontchatbot.SecondActivity.RecyclerView

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.frontchatbot.R

class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val tvCategory: TextView = view.findViewById(R.id.tvCategory)
    private val ibColor: ImageView = view.findViewById(R.id.ibColor)

    fun render(categoryItem: CategoryItem) {
        val context = itemView.context
        when (categoryItem) {
            CategoryItem.Costs -> {
                tvCategory.setText(R.string.category_costs)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_costs_color))
            }
            CategoryItem.Contact -> {
                tvCategory.setText(R.string.category_contact)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_contact_color))
            }
            CategoryItem.Admission -> {
                tvCategory.setText(R.string.category_admission)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_admission_color))
            }
            CategoryItem.Dates -> {
                tvCategory.setText(R.string.category_dates)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_dates_color))
            }
            CategoryItem.Academics -> {
                tvCategory.setText(R.string.category_academics)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_academics_color))
            }
            CategoryItem.Inscription -> {
                tvCategory.setText(R.string.category_inscription)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_inscription_color))
            }
            CategoryItem.Results -> {
                tvCategory.setText(R.string.category_results)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_results_color))
            }
            CategoryItem.Modalities -> {
                tvCategory.setText(R.string.category_modalities)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_modalities_color))
            }
            CategoryItem.StudentLife -> {
                tvCategory.setText(R.string.category_student_life)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_student_life_color))
            }
            CategoryItem.Scholarships -> {
                tvCategory.setText(R.string.category_scholarships)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_scholarships_color))
            }
            CategoryItem.Other -> {
                tvCategory.setText(R.string.category_other)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_other_color))
            }
        }
    }
}