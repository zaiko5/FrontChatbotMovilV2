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
            CategoryItem.Context -> {
                tvCategory.setText(R.string.category_context)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_context_color))
            }
            CategoryItem.Campus -> {
                tvCategory.setText(R.string.category_campus)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_campus_color))
            }
            CategoryItem.Admission -> {
                tvCategory.setText(R.string.category_admission)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_admission_color))
            }
            CategoryItem.Inscription -> {
                tvCategory.setText(R.string.category_inscription)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_inscription_color))
            }
            CategoryItem.Other -> {
                tvCategory.setText(R.string.category_other)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_other_color))
            }
            CategoryItem.WOAnswer -> {
                tvCategory.setText(R.string.category_answers)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.category_woanswer_color))
            }
            //Subtemas
            CategoryItem.Buildings -> {
                tvCategory.setText(R.string.subcategory_buildings)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_buildings_color))
            }
            CategoryItem.Services -> {
                tvCategory.setText(R.string.subcategory_services)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_services_color))
            }
            CategoryItem.Careers -> {
                tvCategory.setText(R.string.subcategory_careers)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_careers_color))
            }
            CategoryItem.GeneralInfo -> {
                tvCategory.setText(R.string.subcategory_general_info)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_general_info_color))
            }
            CategoryItem.Costs -> {
                tvCategory.setText(R.string.subcategory_costs)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_costs_color))
            }
            CategoryItem.ImportantDates -> {
                tvCategory.setText(R.string.subcategory_important_dates)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_important_dates_color))
            }
            CategoryItem.ContactFollowup -> {
                tvCategory.setText(R.string.subcategory_contact_followup)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_contact_followup_color))
            }
            CategoryItem.DocumentsDelivery -> {
                tvCategory.setText(R.string.subcategory_documents_delivery)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_documents_delivery_color))
            }
            CategoryItem.Reenrollment -> {
                tvCategory.setText(R.string.subcategory_reenrollment)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_reenrollment_color))
            }
            CategoryItem.StudyGuide -> {
                tvCategory.setText(R.string.subcategory_study_guide)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_study_guide_color))
            }
            CategoryItem.Graduation -> {
                tvCategory.setText(R.string.subcategory_graduation)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_graduation_color))
            }
            CategoryItem.Scholarships -> {
                tvCategory.setText(R.string.subcategory_scholarships)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_scholarships_color))
            }
            CategoryItem.SchoolControl -> {
                tvCategory.setText(R.string.subcategory_school_control)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_school_control_color))
            }
            CategoryItem.AcademicLoad -> {
                tvCategory.setText(R.string.subcategory_academic_load)
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_academic_load_color))
            }
            CategoryItem.English -> {
                tvCategory.setText(R.string.subcategory_english) // ⚠️ ¡Este string está faltando!
                ibColor.setColorFilter(ContextCompat.getColor(context, R.color.subcategory_english_color))
            }
        }
    }
}