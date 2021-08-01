package ar.edu.unq.eperdemic.vectorTests

import ar.edu.unq.eperdemic.modelo.*
import org.junit.Assert
import org.junit.jupiter.api.Test

class VectorTest {
    private var ubicacion = Ubicacion("Argentina")
    private var vector = Vector(tipo = TipoDeVector.Persona, ubicacion= ubicacion)
    private var especie = Especie(Patogeno("Virus",1,1,1),"Covid", ubicacion)
    @Test
    fun unVectorSabeResponderSuUbicacion() {
        Assert.assertEquals(vector.ubicacion, ubicacion)
    }

    @Test
    fun unVectorSabeResponderSuIdSiLeAsignoUno() {
        vector.id = 1
        Assert.assertEquals(1, vector.id!!.toInt())
    }

    @Test
    fun unVectorSabeResponderTipo() {
        Assert.assertEquals(TipoDeVector.Persona, vector.tipo)
    }

    @Test
    fun unVectorSabeResponderSusEnfermedades() {
        val enfermedades : MutableList<Especie> = mutableListOf()
        Assert.assertEquals(enfermedades, vector.enfermedades)
    }

    @Test
    fun unVectorSabeAgregarUnaEnfermedad() {
        vector.agregarEnfermedad(especie)
        Assert.assertEquals(especie,vector.enfermedades[0])
        Assert.assertEquals(1,vector.enfermedades.size)
    }
}