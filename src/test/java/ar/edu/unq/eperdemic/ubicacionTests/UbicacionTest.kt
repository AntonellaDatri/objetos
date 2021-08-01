package ar.edu.unq.eperdemic.ubicacionTests

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.Vector
import org.junit.Assert
import org.junit.jupiter.api.Test

class UbicacionTest {
    private var ubicacion = Ubicacion("Argentina")
    private var vector = Vector(tipo = TipoDeVector.Persona, ubicacion= ubicacion)

    @Test
    fun unaUbicacionSabeResponderSuNombre() {
        Assert.assertEquals(ubicacion.nombre, "Argentina")
    }

    @Test
    fun unaUbicacionSabeResponderSuIdSiLeAsignoUno() {
        ubicacion.id = 1
        Assert.assertEquals(1, ubicacion.id!!.toInt())
    }

    @Test
    fun unaUbicacionSabeResponderSusEspecies() {
        val especies : MutableList<Especie> = mutableListOf()
        Assert.assertEquals(especies, ubicacion.especies)
    }

    @Test
    fun unaUbicacionSabeResponderSusVectores() {
        val vectores : MutableList<Vector> = mutableListOf(vector)
        Assert.assertEquals(vectores, ubicacion.vectores)
    }

    @Test
    fun unaUbicacionSabeAgregarUnVector() {
        ubicacion.agregarVector(vector)
        Assert.assertEquals(vector,ubicacion.vectores[0])
        Assert.assertEquals(1,ubicacion.vectores.size)
    }
}