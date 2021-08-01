package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Mutacion

interface MutacionDAO {
    fun crear(mutacion: Mutacion): Mutacion
    fun recuperar(id: Long): Mutacion
    fun recuperarTodos(): List<Mutacion>
    fun actualizar(mutacion: Mutacion)
}