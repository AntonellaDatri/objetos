package ar.edu.unq.eperdemic.spring.controllers.dto

import ar.edu.unq.eperdemic.modelo.Especie

class EspecieDTO (val id: Long?,
                  val nombre : String,
                  val patogeno: PatogenoDTO,
                  val paisDeOrigen: UbicacionDTO,
                  val adn:Int,
                 val mutaciones: List<String> ){

    companion object {
        fun desdeModelo(especie:Especie): EspecieDTO = EspecieDTO(especie.id, especie.nombre!!, PatogenoDTO.desdeModelo(especie.patogeno), UbicacionDTO.desdeModelo(especie.paisDeOrigen!!),especie.puntosADN , mutableListOf())
        }

    fun aModelo(): Especie {
        return Especie(patogeno.aModelo(), nombre, paisDeOrigen.aModelo())}

}


