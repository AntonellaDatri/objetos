package ar.edu.unq.eperdemic.spring.controllers.dto

import ar.edu.unq.eperdemic.modelo.Patogeno

class PatogenoDTO (val id:Long?, val tipo : String, val cantidadDeEspecies:Int, val factorContagioHumano: Int,
                   val factorContagioAnimal: Int,
                   val factorContagioInsecto: Int,
                   val defensa: Int,
                   val letalidad: Int){

    companion object {
        fun desdeModelo(patogeno: Patogeno): PatogenoDTO {
            return PatogenoDTO(
                patogeno.id,
                patogeno.toString(),
                patogeno.cantidadDeEspecies,
                patogeno.factorContagioHumano,
                patogeno.factorContagioAnimal,
                patogeno.factorContagioInsecto,
                patogeno.letalidad,
                patogeno.defensa
            )
        }
    }


    fun aModelo(): Patogeno = Patogeno(tipo, factorContagioHumano, factorContagioAnimal, factorContagioInsecto )
}

