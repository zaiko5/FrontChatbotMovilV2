package com.example.frontchatbot.SecondActivity.RecyclerView

//Sealed class para modelar todos los posibles items que se pueden mostrar en el RecyclerView (temas y subtemas)
sealed class CategoryItem() {
    //Temas
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
