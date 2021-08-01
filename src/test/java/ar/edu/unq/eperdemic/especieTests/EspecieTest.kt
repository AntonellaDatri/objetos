package ar.edu.unq.eperdemic.especieTests

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import org.junit.Assert
import org.junit.jupiter.api.Test

class EspecieTest {
    private var virus : Patogeno = Patogeno("Virus",1,1,1)
    private var china: Ubicacion = Ubicacion("China")
    private var coronaVirus : Especie = Especie(virus, "Corona Virus",china)

    @Test
    fun unaEspecieSabeResponderSuNombre() {
        Assert.assertEquals(coronaVirus.nombre, "Corona Virus")
    }

    @Test
    fun unaEspecieConoceASuPatogeno() {
        Assert.assertEquals(coronaVirus.patogeno, virus)
    }

    @Test
    fun unaEspecieSabeResponderSuPaisDeOrigen() {
        val china = Ubicacion("China")
       Assert.assertEquals(coronaVirus.paisDeOrigen!!.nombre, china.nombre!!)
    }

}