package com.tory.jetpack.viewmodel


data class Shoe(
     val name: String
    ,  val description: String
    ,  val price: Float
    , val brand: String
    ,  val imageUrl: String
) {
    var id: Long = 0

    fun getPriceStr():String{
        return price.toString()
    }
}