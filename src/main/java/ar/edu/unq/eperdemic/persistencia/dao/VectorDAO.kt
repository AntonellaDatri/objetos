package ar.edu.unq.eperdemic.persistencia.dao

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Vector

interface VectorDAO {
    fun crear(vector: Vector): Vector
    fun recuperar(vectorId: Long): Vector
    fun actualizar(vector: Vector)
    fun recuperarTodos(): List<Vector>
}