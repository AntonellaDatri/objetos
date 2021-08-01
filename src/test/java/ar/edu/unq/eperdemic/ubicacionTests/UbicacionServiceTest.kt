package ar.edu.unq.eperdemic.ubicacionTests

import ar.edu.unq.eperdemic.modelo.*
import ar.edu.unq.eperdemic.persistencia.dao.*
import ar.edu.unq.eperdemic.persistencia.dao.hibernate.*
import ar.edu.unq.eperdemic.persistencia.dao.mongoDB.MongoDBEventoDAO
import ar.edu.unq.eperdemic.persistencia.dao.neo4j.UbicacionNeo4jDAO
import ar.edu.unq.eperdemic.services.EspecieService
import ar.edu.unq.eperdemic.services.PatogenoService
import ar.edu.unq.eperdemic.services.UbicacionService
import ar.edu.unq.eperdemic.services.VectorService
import ar.edu.unq.eperdemic.services.impl.EspecieServiceImpl
import ar.edu.unq.eperdemic.services.impl.PatogenoServiceImpl
import ar.edu.unq.eperdemic.services.impl.UbicacionServiceImpl
import ar.edu.unq.eperdemic.services.impl.VectorServiceImpl
import org.hibernate.exception.ConstraintViolationException
import org.junit.Assert
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertThrows

class UbicacionServiceTest {
    private val dataDAO : DataDAO = HibernateDataDAO()
    private val hibenateUbicacionDAO: UbicacionDAO = HibernateUbicacionDAO()
    private val neo4jUbicacionDAO =  UbicacionNeo4jDAO()
    private val especieDAO : EspecieDAO = HibernateEspecieDAO()
    private val vectorDAO: VectorDAO = HibernateVectorDAO()
    private val patogenoDAO: PatogenoDAO = HibernatePatogenoDAO()
    private val eventoDAO = MongoDBEventoDAO()

    private val ubicacionService : UbicacionService = UbicacionServiceImpl(neo4jUbicacionDAO,hibenateUbicacionDAO, dataDAO, vectorDAO, eventoDAO)

    val vectorService : VectorService = VectorServiceImpl(vectorDAO, dataDAO,especieDAO,hibenateUbicacionDAO, eventoDAO)
    private val especieService: EspecieService = EspecieServiceImpl(especieDAO, dataDAO, hibenateUbicacionDAO,vectorDAO,eventoDAO)
    private val patogenoService : PatogenoService = PatogenoServiceImpl(patogenoDAO, dataDAO, eventoDAO)
    private lateinit var vector : Vector
    private lateinit var  ubicacion: Ubicacion

    @BeforeEach
    fun start() {
        ubicacionService.clear()
        neo4jUbicacionDAO.clear()
    }
//    Testeo si se crea una ubicacion correctamente y recupererla
    @Test
    fun seCreanYSeRecuperanUbicacionesCorrectamente() {
        ubicacion = ubicacionService.crear("Argentina")
        Assert.assertEquals(ubicacionService.recuperar(ubicacion.id!!).nombre, "Argentina")
    }

    @Test
    fun sePuedenConectarDosUbicaciones() {
        val argentina = ubicacionService.crear("Argentina")
        val china = ubicacionService.crear("China")
        ubicacionService.conectar(argentina.id!!, china.id!!, "Terrestre")
        Assert.assertEquals(1, ubicacionService.conectados(argentina.id!!).size)
        Assert.assertEquals(china.nombre, ubicacionService.conectados(argentina.id!!)[0].nombre)
        Assert.assertEquals(0, ubicacionService.conectados(china.id!!).size)
    }

    @Test
    fun sePuedenConectarMasDeDosUbicaciones() {
        val argentina = ubicacionService.crear("Argentina")
        val espania = ubicacionService.crear("España")
        val china = ubicacionService.crear("China")

        ubicacionService.conectar(argentina.id!!, china.id!!, "Terrestre")
        ubicacionService.conectar(argentina.id!!, espania.id!!, "Aereo")

        Assert.assertEquals(2, ubicacionService.conectados(argentina.id!!).size)
        Assert.assertTrue(ubicacionService.conectados(argentina.id!!).any { u -> u.nombre == china.nombre })
        Assert.assertTrue(ubicacionService.conectados(argentina.id!!).any { u -> u.nombre == espania.nombre })
    }

    @Test
    fun noSePuedenConectarDosUbicacionesSiUnaNoExiste() {
        val argentina = ubicacionService.crear("Argentina")
        ubicacionService.conectar(argentina.id!!, 2, "Terrestre")
        Assert.assertEquals(0, ubicacionService.conectados(argentina.id!!).size)
    }

    @Test
    fun unaUbicacionNoSePuedenConectarASiMisma() {
        val argentina = ubicacionService.crear("Argentina")
        ubicacionService.conectar(argentina.id!!, argentina.id!!, "Terrestre")
        Assert.assertEquals(0, ubicacionService.conectados(argentina.id!!).size)
    }

    @Test
    fun sePuedenConectarDosUbicacionesBilateralmente() {
        val argentina = ubicacionService.crear("Argentina")
        val china = ubicacionService.crear("China")

        ubicacionService.conectar(argentina.id!!, china.id!!, "Terrestre")
        ubicacionService.conectar(china.id!!, argentina.id!!, "Terrestre")

        Assert.assertEquals(1, ubicacionService.conectados(argentina.id!!).size)
        Assert.assertEquals(1, ubicacionService.conectados(china.id!!).size)
    }

    @Test
    fun noSePuedeMoverPorqueNoPuedeIrPorEseTipoDeCamino() {
        val argentina = ubicacionService.crear("Argentina")
        val china = ubicacionService.crear("China")

        ubicacionService.conectar(argentina.id!!, china.id!!, "Maritimo")

        Assertions.assertThrows(UbicacionNoAlcanzable::class.java) { ubicacionService.mover(vectorService.crear(TipoDeVector.Insecto,argentina.id!!).id!!, china.id!!) }
    }

    @Test
    fun unaUnPatogenoQueEsteEnUnaPersonaPuedeMoverseSoloPorLosCaminosCorrectos() {
        val stBlah = ubicacionService.crear("St.Blah")
        val plantala = ubicacionService.crear("Plantala")
        val agualan = ubicacionService.crear("Agualan...")
        val tibetDojo = ubicacionService.crear("Tibet Dojo")
        val bicholan = ubicacionService.crear("Bicholan...")
        val persona = vectorService.crear(TipoDeVector.Persona,stBlah.id!!)

        ubicacionService.conectar(stBlah.id!!, agualan.id!!, "Terrestre")
        ubicacionService.conectar(stBlah.id!!, plantala.id!!, "Aereo")
        ubicacionService.conectar(plantala.id!!, agualan.id!!, "Maritimo")
        ubicacionService.conectar(tibetDojo.id!!, plantala.id!!, "Terrestre")
        ubicacionService.conectar(tibetDojo.id!!, bicholan.id!!, "Aereo")
        ubicacionService.conectar(agualan.id!!, stBlah.id!!, "Terrestre")
        ubicacionService.conectar(agualan.id!!, plantala.id!!, "Maritimo")
        ubicacionService.conectar(agualan.id!!, bicholan.id!!, "Maritimo")
        ubicacionService.conectar(bicholan.id!!, tibetDojo.id!!, "Aereo")

        Assert.assertEquals(3, ubicacionService.capacidadDeExpansion(persona.id!!, persona.ubicacion.nombre!!, 3))
    }

    @Test
    fun unaUnPatogenoQueEsteEnUnAnimalPuedeMoverseSoloPorLosCaminosCorrectos() {
        val stBlah = ubicacionService.crear("St.Blah")
        val plantala = ubicacionService.crear("Plantala")
        val agualan = ubicacionService.crear("Agualan...")
        val tibetDojo = ubicacionService.crear("Tibet Dojo")
        val bicholan = ubicacionService.crear("Bicholan...")
        val persona = vectorService.crear(TipoDeVector.Animal,stBlah.id!!)

        ubicacionService.conectar(stBlah.id!!, agualan.id!!, "Terrestre")
        ubicacionService.conectar(stBlah.id!!, plantala.id!!, "Aereo")
        ubicacionService.conectar(plantala.id!!, agualan.id!!, "Maritimo")
        ubicacionService.conectar(tibetDojo.id!!, plantala.id!!, "Terrestre")
        ubicacionService.conectar(tibetDojo.id!!, bicholan.id!!, "Aereo")
        ubicacionService.conectar(agualan.id!!, stBlah.id!!, "Terrestre")
        ubicacionService.conectar(agualan.id!!, plantala.id!!, "Maritimo")
        ubicacionService.conectar(agualan.id!!, bicholan.id!!, "Maritimo")
        ubicacionService.conectar(bicholan.id!!, tibetDojo.id!!, "Aereo")

        Assert.assertEquals(4, ubicacionService.capacidadDeExpansion(persona.id!!, persona.ubicacion.nombre!!, 3))
    }

    @Test
    fun unaUnPatogenoQueEsteEnUnInsectoPuedeMoverseSoloPorLosCaminosCorrectos() {
        val stBlah = ubicacionService.crear("St.Blah")
        val plantala = ubicacionService.crear("Plantala")
        val agualan = ubicacionService.crear("Agualan...")
        val tibetDojo = ubicacionService.crear("Tibet Dojo")
        val bicholan = ubicacionService.crear("Bicholan...")
        val persona = vectorService.crear(TipoDeVector.Insecto,stBlah.id!!)

        ubicacionService.conectar(stBlah.id!!, agualan.id!!, "Terrestre")
        ubicacionService.conectar(stBlah.id!!, plantala.id!!, "Aereo")
        ubicacionService.conectar(plantala.id!!, agualan.id!!, "Maritimo")
        ubicacionService.conectar(tibetDojo.id!!, plantala.id!!, "Terrestre")
        ubicacionService.conectar(tibetDojo.id!!, bicholan.id!!, "Aereo")
        ubicacionService.conectar(agualan.id!!, stBlah.id!!, "Terrestre")
        ubicacionService.conectar(agualan.id!!, plantala.id!!, "Maritimo")
        ubicacionService.conectar(agualan.id!!, bicholan.id!!, "Maritimo")
        ubicacionService.conectar(bicholan.id!!, tibetDojo.id!!, "Aereo")

        Assert.assertEquals(2, ubicacionService.capacidadDeExpansion(persona.id!!, persona.ubicacion.nombre!!, 3))
    }


//    Testeo si puedo Recuperar a todas las ubicaciones
    @Test
    fun puedoRecuperarTodasLasUbicaciones() {
        ubicacionService.crear("Argentina")
        ubicacionService.crear("Chile")
        ubicacionService.crear("Brasil")
        ubicacionService.crear("Paraguay")

        Assert.assertEquals(ubicacionService.recuperarTodos().size, 4)
    }

//    Testeo que no puedo <crear dos ubicaciones con el mismo nombre
    @Test
    fun noPuedoCrearDosUbicacionesConElMismoNombre() {
        ubicacionService.crear("Argentina")
        assertThrows(ConstraintViolationException::class.java)  { ubicacionService.crear("Argentina") }
    }

//    Testeo que un vector no infecta a nadie si no hay nadie en esa ubicacion
    @Test
    fun cuandoSeMueveUnVectorInfectaAtodosAUnaUbicacionSinVectoresElUnicoVectorInfectadoEsElMismo() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)
        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Maritimo")
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)
        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)

        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,1)
        for (vector in vectoresEnUbicacion){
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        }
    }

//    Testeo que un vector con mas de una enfermedad infecta a otro vector con todas las enfermedades
    @Test
    fun cuandoSeMueveUnVectorConMasDeUnaEnfermedadYElPorcentajeDeInfeccionEs100InfectaAtodosLosQueEstanEnEsaUbicacionConTodasLasEnfermedades() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val virus2 = Patogeno("virus2",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        val personaVector = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)
        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Maritimo")
        //se mueve un vector sano
        //ubicacionService.mover(personaVector.id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarPorNombre("esterichiaColi")
        patogenoService.crear(virus2)
        patogenoService.agregarEspecie(virus2.id!!,"corona",ubicacion.id!!)
        val corona = especieService.recuperarPorNombre("corona")

        vectorService.infectar(personaVector.id!!, esterichiaColi.id!!)
        vectorService.infectar(personaVector.id!!, corona.id!!)
        vector = vectorService.recuperar(personaVector.id!!)

        Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(corona.id))
        Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(corona.nombre))
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(2, vectoresEnUbicacion.size)
        for (vector in vectoresEnUbicacion){
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(corona.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(corona.nombre))
        }
    }

//    Testeo que un vector no infecta a ningun otro vector si el porcentaje es 1
    @Test
    fun cuandoSeMueveUnVectorYElPosentajeDeInfeccionEs1NoInfectaALosQueEstanEnEsaUbicacion() {
        RandomNumber.strategy = NumeroSeteadoStrategy(1)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        val vectorAInfectar =vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Maritimo")

        ubicacionService.mover(vectorAInfectar.id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        Assert.assertFalse(vectorAInfectar.estaInfectado())
    }

//    Testeo que una Persona se mueva E infecte a otras personas e insectos pero no a animales
    @Test
    fun cuandoSeMueveUnaPersonaYElPorcentajeDeInfeccionEs100InfectaAtodasLasPersonasEInsectosQueEstanEnEsaUbicacion() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Terrestre")

        ubicacionService.mover(vectorService.crear(TipoDeVector.Persona,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        ubicacionService.mover(vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,3)
        for (vector in vectoresEnUbicacion){
            val enfermedades =vector.enfermedades
            Assert.assertTrue(enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        }
    }

    @Test
    fun cuandoSeMueveUnaPersonaYElPorcentajeDeInfeccionEs100NoInfectaALosAnimales() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        val vectorAInfectar =vectorService.crear(TipoDeVector.Animal,ubicacion.id!!)
        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Maritimo")
        ubicacionService.mover(vectorAInfectar.id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,2)
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
    }

//    Testeo que un Animal se mueva E infecte a personas e insectos pero no a animales
    @Test
    fun cuandoSeMueveUnAnimalYElPorcentajeDeInfeccionEs100InfectaAtodasLasPersonasEInsectosQueEstanEnEsaUbicacion() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Terrestre")

        ubicacionService.mover(vectorService.crear(TipoDeVector.Persona,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        ubicacionService.mover(vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Animal,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,3)
        for (vector in vectoresEnUbicacion){
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        }
    }

    @Test
    fun cuandoSeMueveUnAnimalYElPorcentajeDeInfeccionEs100NoInfectaALosAnimales() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        val vectorAInfectar =vectorService.crear(TipoDeVector.Animal,ubicacion.id!!)
        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Maritimo")
        ubicacionService.mover(vectorAInfectar.id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Animal,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,2)
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
    }

//    Testeo que un Insecto se mueva E infecte a personas y animales pero no a insectos
    @Test
    fun cuandoSeMueveUnInsectoYElPorcentajeDeInfeccionEs100InfectaAtodasLasPersonasYAnimalesQueEstanEnEsaUbicacion() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Terrestre")

        ubicacionService.mover(vectorService.crear(TipoDeVector.Persona,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        ubicacionService.mover(vectorService.crear(TipoDeVector.Animal,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,3)
        for (vector in vectoresEnUbicacion){
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        }
    }

    @Test
    fun cuandoSeMueveUnInsectoYElPorcentajeDeInfeccionEs100NoInfectaAOtrosInsectos () {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)

        val virus = Patogeno("virus",100,100,100)
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")
        val vectorAInfectar =vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!)

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Aereo")

        ubicacionService.mover(vectorAInfectar.id!!, ubicacionAMover.id!!)
        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)
        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertEquals(vectoresEnUbicacion.size,2)
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertFalse(vectorAInfectar.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
    }


 //Testeo que dada una ubicación, toma un vector contagiado y contagia a todos los vectores con las enfermedades del vector.
    @Test
    fun cuandoSeEligeUnaUbicacionSeEligeunVectorInfectadoYSeContagiaATodosLosVectoresQueEstanEnEsaUbicacionConTodasLasEnfermedadesDelVectorContagiado() {
        RandomNumber.strategy = NumeroSeteadoStrategy(10)
         val virus = Patogeno("virus", 100,100,100)
         val ubicacion=  ubicacionService.crear("China")
         val ubicacionAMover=  ubicacionService.crear("Argentina")
         val vectorAInfectar1 = vectorService.crear(TipoDeVector.Persona,ubicacion.id!!)
         val vectorAInfectar2 = vectorService.crear(TipoDeVector.Animal,ubicacion.id!!)

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Terrestre")

        patogenoService.crear(virus)
        patogenoService.agregarEspecie(virus.id!!,"esterichiaColi",ubicacion.id!!)
        val esterichiaColi = especieService.recuperarTodos()[0]
        vector = vectorService.crear(TipoDeVector.Insecto,ubicacion.id!!)

        vectorService.infectar(vector.id!!, esterichiaColi.id!!)
        vector = vectorService.recuperar(vector.id!!)

        ubicacionService.mover(vector.id!!, ubicacionAMover.id!!)
        ubicacionService.mover(vectorAInfectar1.id!!, ubicacionAMover.id!!)
        ubicacionService.mover(vectorAInfectar2.id!!, ubicacionAMover.id!!)

        Assert.assertFalse(vectorAInfectar1.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertFalse(vectorAInfectar2.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))

        RandomNumber.strategy = NumeroSeteadoStrategy(0)
        ubicacionService.expandir(ubicacionAMover.id!!)

        val vectoresEnUbicacion = ubicacionService.recuperar(ubicacionAMover.id!!).vectores

        Assert.assertFalse(vectorAInfectar1.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertFalse(vectorAInfectar2.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
        Assert.assertEquals(vectoresEnUbicacion.size,3)

     for (vector in vectoresEnUbicacion){
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.id }.contains(esterichiaColi.id))
            Assert.assertTrue(vector.enfermedades.map { esp -> esp.nombre }.contains(esterichiaColi.nombre))
        }
    }

    @Test
    fun probando(){
        val ubicacion=  ubicacionService.crear("China")
        val ubicacionAMover=  ubicacionService.crear("Argentina")

        neo4jUbicacionDAO.conectar(ubicacion.id!!, ubicacionAMover.id!!, "Terrestre")

        ubicacionService.mover(vectorService.crear(TipoDeVector.Persona,ubicacion.id!!).id!!, ubicacionAMover.id!!)
        Assert.assertTrue(true)
    }

    @AfterEach
    fun clear() {
       RandomNumber.strategy = NumeroRamdomStategy()
        ubicacionService.clear()
        neo4jUbicacionDAO.clear()

    }

}