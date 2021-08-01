package ar.edu.unq.eperdemic.patogenoTests

import ar.edu.unq.eperdemic.modelo.Patogeno
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import org.junit.Assert
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test



class PatogenoServiceTest {
    private val service: PatogenoService = PatogenoServiceImpl(
        HibernatePatogenoDAO(), HibernateDataDAO(), MongoDBEventoDAO()
    )
    private lateinit var patogeno1: Patogeno
    private lateinit var ubicacionService: UbicacionService

    @BeforeEach
    fun beforeAll(){
        service.clear()
        this.ubicacionService = UbicacionServiceImpl(UbicacionNeo4jDAO(),
            HibernateUbicacionDAO(),
            HibernateDataDAO(), HibernateVectorDAO(),  MongoDBEventoDAO())
    }

    @Test
    fun alGuardarYLuegoRecuperarSeObtieneObjetosSimilares() {
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        val otroPatogeno = service.recuperar(patogeno1.id!!)
        Assert.assertEquals(patogeno1.id, otroPatogeno.id)
        Assert.assertEquals(patogeno1.tipo, otroPatogeno.tipo)
        Assert.assertEquals(patogeno1.cantidadDeEspecies, otroPatogeno.cantidadDeEspecies)
        Assert.assertTrue(patogeno1 !== otroPatogeno)
    }


    @Test
    fun agregoPatogenoAListaVaciaYAlRecuperarHayUno(){
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        val patogenos = service.recuperarTodos()
        Assert.assertEquals(1, patogenos.size)
    }

    @Test
    fun patogenoAgregadoSeEncuentraEnArrayDeTodosLosPatogenos(){
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        val patogenos = service.recuperarTodos()
        Assert.assertEquals(patogeno1.id, patogenos[0].id)
        Assert.assertEquals(patogeno1.tipo, patogenos[0].tipo)
        Assert.assertEquals(patogeno1.cantidadDeEspecies, patogenos[0].cantidadDeEspecies)
    }


    @Test
    fun agregoEspecieAPatogenoYSeActualizaArrayDeEspecies(){
        val pandora =  ubicacionService.crear("Pandora")
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        service.agregarEspecie(patogeno1.id!!, "coco", pandora.id!!)
        val patogenoCreated = service.recuperar(patogeno1.id!!)
        Assert.assertEquals(1, patogenoCreated.especies.size)
        Assert.assertEquals(patogeno1.id, patogenoCreated.especies[0].patogeno.id)
        Assert.assertEquals(pandora.nombre, patogenoCreated.especies[0].paisDeOrigen!!.nombre)
    }


    @Test
    fun agregoEspecieAPatogenoYSeIncrementaCantidadDeEspecies(){
        val pandora =  ubicacionService.crear("Pandora")
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        service.agregarEspecie(patogeno1.id!!, "coco", pandora.id!!)
        Assert.assertEquals(1, service.recuperar(patogeno1.id!!).cantidadDeEspecies)
    }

    @Test
    fun arrayVacioDeEspeciesAlInicializar(){
        patogeno1 = service.crear(Patogeno("Bacteria",1,1,1))
        Assert.assertTrue(patogeno1.especies.isEmpty())
    }

    @Test
    fun alBuscarEnLaBDVaciaTodosLosPatogenosDevuelveUnaListaVacia(){
        val patogenos = service.recuperarTodos()
        val list : List<Patogeno> = listOf()
        Assert.assertEquals(patogenos, list)
    }

    @Test
    fun alRecuperarPorUnIdQueNoExisteObtengoNull() {
        // Confirmo que no hay un patogenos en la bd
        val patogenos = service.recuperarTodos()
        Assert.assertEquals(patogenos.size, 0)
        // Creo un id falso
        val id = 1
        // Busco recuperar un patogeno con el id inexistente
        Assert.assertEquals(null, service.recuperar(id.toLong()))
    }

    @AfterEach
    fun clear(){
        service.clear()
    }

}