package ar.edu.unq.eperdemic.modelo
import ar.edu.unq.eperdemic.persistencia.dao.*


class ContagiarVectorStrategy{
    private var strategy : VectorStrategy? = null

    fun contagiarVector(vectorAContagiar: Vector, vectorContagiado: Vector,  especie : Especie, number: Int, especiesEnUbicacion: List<Especie>, eventoDAO: EventoDAO){
        when (vectorAContagiar.tipo) {
            TipoDeVector.Persona -> {
                strategy = PersonaStrategy()
                strategy!!.enfermar(vectorContagiado, vectorAContagiar, especie, number, especiesEnUbicacion, eventoDAO)
            }
            TipoDeVector.Animal -> {
                strategy = AnimalStrategy()
                strategy!!.enfermar(vectorContagiado, vectorAContagiar, especie, number, especiesEnUbicacion, eventoDAO)
            }
            TipoDeVector.Insecto -> {
                strategy = InsectoStrategy()
                strategy!!.enfermar(vectorContagiado, vectorAContagiar, especie, number, especiesEnUbicacion, eventoDAO)
            }
        }
    }
}

abstract class VectorStrategy {
    abstract fun seContagia(vectorContagiado:Vector, patogeno: Patogeno, number: Int) : Boolean

    fun enfermar(vectorContagiado : Vector, vectorAContagiar : Vector, especie : Especie, number: Int, especiesEnUbicacion: List<Especie>, eventoDAO: EventoDAO) {

        //Aca verifico si se contagia y la especie esta en la ubicacion
        if (seContagia(vectorContagiado, especie.patogeno, number) && !especiesEnUbicacion.contains(especie)){
            val eventoPatogeno= EventoFeedPatogeno(especie.patogeno.tipo!!, "Contagio", "Contagio")
            val eventoVector= EventoFeedVector(vectorContagiado.id!!,vectorContagiado.tipo, "Contagió", "Vector", vectorContagiado.ubicacion.nombre!!)
            val eventoUbicacion = EventoFeedUbicacion(vectorAContagiar.ubicacion.id!!, vectorAContagiar.id!!,vectorAContagiar.tipo ,"ContagioUbicacion", "Contagio")
            eventoDAO.guardar(eventoPatogeno)
            eventoDAO.guardar(eventoVector)
            eventoDAO.guardar(eventoUbicacion)
            vectorAContagiar.agregarEnfermedad(especie)
            val eventoEnfermar = EventoFeedVector(vectorAContagiar.id!!,vectorAContagiar.tipo, "Enfermedades", "Vector", vectorContagiado.ubicacion.nombre!!)
            eventoDAO.guardar(eventoEnfermar)
        }

        else if (seContagia(vectorContagiado, especie.patogeno, number) ){
            vectorAContagiar.agregarEnfermedad(especie)
        }
    }

    fun fueContagiado(porcentaje : Int, number: Int) : Boolean {
        val posibilidades : MutableList<Boolean> = mutableListOf()

        return if (porcentaje >= 100) {
            true
        } else {

            val hasta = 100 -porcentaje
            repeat (hasta){
                posibilidades.add(false)
            }
            repeat (porcentaje){
                posibilidades.add(true)
            }
            posibilidades[number]
        }
    }
}

class PersonaStrategy : VectorStrategy() {
   override fun seContagia(vectorContagiado:Vector, patogeno: Patogeno, number: Int) : Boolean {
       val porcentaje = number + patogeno.factorContagioHumano
       return fueContagiado(porcentaje, number)
    }
}

class AnimalStrategy :VectorStrategy() {
    override fun seContagia(vectorContagiado:Vector, patogeno: Patogeno, number: Int) : Boolean {
        val porcentaje = number + patogeno.factorContagioAnimal
        return vectorContagiado.tipo == TipoDeVector.Insecto  && fueContagiado(porcentaje, number)
    }
}

class InsectoStrategy :VectorStrategy() {
    override fun seContagia(vectorContagiado:Vector, patogeno: Patogeno, number: Int) : Boolean {
        val porcentaje = number + patogeno.factorContagioInsecto
        return (vectorContagiado.tipo == TipoDeVector.Persona || vectorContagiado.tipo == TipoDeVector.Animal)  && fueContagiado(porcentaje, number)
    }
}
