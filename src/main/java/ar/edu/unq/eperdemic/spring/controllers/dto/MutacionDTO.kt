package ar.edu.unq.eperdemic.spring.controllers.dto

import ar.edu.unq.eperdemic.modelo.Mutacion

enum class Atributo {
    LETALIDAD,
    DEFENSA ,
    FACTOR_ANIMAL,
    FACTOR_INSECTO,
    FACTOR_HUMANO;
}

class MutacionDTO (val id: Long?,
                   val nombre : String,
                   val adnRequeridos: Int,
                   val atributo: Atributo,
                   val cantidad: Int ){


    companion object {
        fun desdeModelo(mutacion:Mutacion): MutacionDTO {
            return MutacionDTO(mutacion.id,mutacion.nombre!!,mutacion.puntosDeADNRequeridos,Atributo.DEFENSA,mutacion.puntosDeADNRequeridos)
        }
    }

    fun aModelo():Mutacion {
            return Mutacion(nombre, mutableListOf(),adnRequeridos)
    }

}


