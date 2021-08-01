package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno

interface PatogenoDAO {
    fun crear(patogeno: Patogeno): Patogeno
    fun recuperar(idDelPatogeno: Long): Patogeno
    fun recuperarPorTipo(tipo: String): Patogeno
    fun recuperarATodos() : List<Patogeno>
    fun actualizar(patogeno: Patogeno)
}