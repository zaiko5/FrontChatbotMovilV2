package com.example.frontchatbot.SecondActivity.RecyclerView

sealed class CategoryItem() {
    object Context : CategoryItem()
    object Campus : CategoryItem()
    object Admission : CategoryItem()
    object Inscription : CategoryItem()
    object Other : CategoryItem()
    object WOAnswer : CategoryItem()

    //Objetos para los subtemas
    object Buildings : CategoryItem()
    object Services : CategoryItem()
    object Careers : CategoryItem()
    object GeneralInfo : CategoryItem()
    object Costs : CategoryItem()
    object ImportantDates : CategoryItem()
    object ContactFollowup : CategoryItem()
    object DocumentsDelivery : CategoryItem()
    object Reenrollment : CategoryItem()
    object StudyGuide : CategoryItem()
    object Graduation : CategoryItem()
    object Scholarships : CategoryItem()
    object SchoolControl : CategoryItem()
    object AcademicLoad : CategoryItem()
    object English : CategoryItem()
}
