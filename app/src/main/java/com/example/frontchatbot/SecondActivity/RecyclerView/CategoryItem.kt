package com.example.frontchatbot.SecondActivity.RecyclerView

sealed class CategoryItem() {
    object Admission : CategoryItem()
    object Dates : CategoryItem()
    object Academics : CategoryItem()
    object Inscription : CategoryItem()
    object Results : CategoryItem()
    object Costs : CategoryItem()
    object Modalities : CategoryItem()
    object Contact : CategoryItem()
    object StudentLife : CategoryItem()
    object Scholarships : CategoryItem()
    object Other : CategoryItem()
}
