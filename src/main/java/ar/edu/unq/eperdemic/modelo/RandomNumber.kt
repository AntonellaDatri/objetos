package ar.edu.unq.eperdemic.modelo

import kotlin.random.Random

object RandomNumber {
    var strategy : RamdomStrategy = NumeroRamdomStategy()

    fun rango(desde: Int, hasta: Int) : Int{
        return strategy.rango(desde,hasta)
    }

}

abstract class RamdomStrategy{
    abstract var numero : Int
    abstract fun rango(desde: Int, hasta: Int) : Int

}

class NumeroRamdomStategy() : RamdomStrategy(){
    override var numero: Int = 0
    override fun rango(desde: Int, hasta: Int) : Int  {
        return Random.nextInt(desde, hasta)
    }

}

class NumeroSeteadoStrategy(override var numero: Int) : RamdomStrategy(){
    override fun rango(desde: Int, hasta: Int): Int {
        return numero
    }

}





