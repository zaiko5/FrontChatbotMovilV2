package com.example.frontchatbot.SecondActivity.RecyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frontchatbot.R

//Adapter para el RecyclerView de las categorias.
class CategoryAdapter(val categories: List<CategoryItem>) : RecyclerView.Adapter<CategoryViewHolder>() {

    //Funcion que crea una vista para cada elemento del listado.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false) //Creando instancia de la vista e inflandola con el layout que se creo de tareas.
        return CategoryViewHolder(view) //Retornamos el viewholder con la vista inflada.
    }

    //Funcion que le pasa al viewholder cada uno de los elementos del listado.
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.render(categories[position]) //Pasamos la tarea al viewholder, desde la actividad principal se le pasa la lista de tareas y en esta funcion se iteran para agregar estilo a cada elemento.
    }

    //Definir el tamaño del listado a mostrar en el RV.
    override fun getItemCount() = categories.size
}