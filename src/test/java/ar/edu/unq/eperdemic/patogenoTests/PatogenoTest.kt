package ar.edu.unq.eperdemic.patogenoTests

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import org.junit.Assert
import org.junit.jupiter.api.Test


class PatogenoTest {
    private var virus : Patogeno = Patogeno("Virus",1,1,1)


    @Test
    fun unPatogenoSabeResponderSuTipo() {
        Assert.assertEquals(virus.tipo, "Virus")
    }

    @Test
    fun unPatogenoNoTieneUnIdSiNoSeLeAsigna() {
        Assert.assertEquals(virus.id, null)
    }

    @Test
    fun siLeAsignoUnIdAlPatogenoSabeResponderSuId() {
        virus.id = 1
        Assert.assertEquals(virus.id!!.toInt(), 1)
    }

    @Test
    fun unPatogenoEsCreadoSinNingunaEspecie() {
        Assert.assertEquals(virus.especies.size, 0)
    }

    @Test
    fun unPatogenoPuedeCrearUnaEspecieYGuardarla() {
        val china = Ubicacion("China")
        virus.crearEspecie("Corona", china)
        Assert.assertEquals(virus.especies.size, 1)
    }

}