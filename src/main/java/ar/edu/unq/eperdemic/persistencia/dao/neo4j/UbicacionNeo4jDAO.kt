package ar.edu.unq.eperdemic.persistencia.dao.neo4j
import ar.edu.unq.eperdemic.modelo.Ubicacion
import ar.edu.unq.eperdemic.modelo.UbicacionResumida
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.NeoUbicacionDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionNoAlcanzable
import org.neo4j.driver.*

open class UbicacionNeo4jDAO : NeoUbicacionDAO , Neo4jDAO(){

    override fun crear(ubicacionId: Long?, nombreUbicacion: String) {
        driver.session().use {session ->
           session.writeTransaction {
               val query= """ 
                   CREATE( ubicacion : UbicacionResumida {id: ${'$'}ubicacionId, nombre: ${'$'}nombreDeUbicacion })
               """
               it.run(query,Values.parameters( "ubicacionId",ubicacionId, "nombreDeUbicacion", nombreUbicacion))
           }
       }
    }

    // que conecte dos ubicaciones (se asumen preexistentes) por medio de un tipo de camino.
    override fun conectar(ubicacion1:Long, ubicacion2:Long, tipoCamino:String) {
        driver.session().use {session ->
            val query= """
                    MATCH (ubicacion1: UbicacionResumida {id: ${'$'}ubicacionId1})
                    MATCH (ubicacion2: UbicacionResumida {id: ${'$'}ubicacionId2})
                    WHERE ubicacion1.nombre <> ubicacion2.nombre
                    MERGE (ubicacion1)-[:${tipoCamino}]->(ubicacion2)
                """
            session.run(
                query,Values.parameters(
                    "ubicacionId1", ubicacion1,
                    "ubicacionId2", ubicacion2
                )
            )
        }
    }


    //mover a ese vector por el camino mas corto. si no puede llegar tira excepcion
    /*override fun mover(caminos: MutableList<String>, ubicacionid: Long) : List<UbicacionResumida>{
        val strCaminos =  caminos.toString().replace("[", "[:").replace(", ", "|")
        return driver.session().use { session ->
            val query = """
                    MATCH (ubicacion:UbicacionResumida {id: ${'$'}ubicacionId })<-${strCaminos}-(ubicacionConectada:UbicacionResumida)
                    WHERE ubicacion.nombre <> ubicacionConectada.nombre
                    RETURN ubicacionConectada
                """
            val result = session.run(query, Values.parameters("ubicacionId", ubicacionid))
            result.list { record: Record ->
                val ubicacionConectada = record[0]
                val id = ubicacionConectada["id"].asLong()
                val nombre = ubicacionConectada["nombre"].asString()
                UbicacionResumida(id, nombre)
            }
        }
    }*/

    override fun mover(caminos: MutableList<String>, ubicacion: Ubicacion, vector:Vector): Int{
        val strCaminos =  caminos.toString().replace("[", "[:").replace("]", "*]").replace(", ", "|")
        try {
            return driver.session().use { session ->
                val query = """
                    MATCH (actual:UbicacionResumida {id: ${'$'}actualId} ),
                          (ubicacion:UbicacionResumida {id: ${'$'}ubicacionId}),
                           p = shortestPath((actual)-${strCaminos}->(ubicacion))
                    RETURN length(p)
                """
                val result = session.run(query, Values.parameters("actualId", vector.ubicacion!!.id, "ubicacionId", ubicacion.id ))
                result.single().get(0).asInt()
            }
        }catch (e:Exception){
            throw UbicacionNoAlcanzable()
        }

    }

    //Retorne todos las ubicaciones conectadas al a ubicación dada por cualquier tipo de camino.
    override fun conectados(ubicacionId:Long): List<UbicacionResumida> {
        return driver.session().use { session ->
            val query = """
                 MATCH (ubicacion:UbicacionResumida {id: ${'$'}ubicacionId })-[:Aereo|:Terrestre|:Maritimo]->(ubicacionConectada:UbicacionResumida)
                RETURN ubicacionConectada
            """
            val result = session.run(query, Values.parameters("ubicacionId", ubicacionId))
            result.list { record: Record ->
                val ubicacionConectada = record[0]
                val id = ubicacionConectada["id"].asLong()
                val nombre = ubicacionConectada["nombre"].asString()
                UbicacionResumida(id, nombre)
            }
        }
    }

    //que dado un vector, retorna la cantidad de diferentes
// ubicaciones a las que podría moverse el Vector dada una cierta cantidad de movimientos.
    override fun capacidadDeExpansion(caminos:MutableList<String>, nombreDeUbicacion:String, movimientos:Int): Int{
        val strCaminos =  caminos.toString().replace("[", "[:").replace(", ", "|")
        val relacionQuery = strCaminos.replace("]", "*0..$movimientos]")
        return driver.session().use { session ->
                val query = """
                    MATCH (ubicacion:UbicacionResumida {nombre: ${'$'}nombreDeUbicacion })-${relacionQuery}->(ubicacionConectada:UbicacionResumida)
                    WHERE ubicacion.nombre <> ubicacionConectada.nombre
                    RETURN DISTINCT ubicacionConectada
                """
                val result = session.run(query, Values.parameters("nombreDeUbicacion", nombreDeUbicacion))
                result.list { record: Record ->
                    val ubicacionConectada = record[0]
                    val id = ubicacionConectada["id"].asLong()
                    val nombre = ubicacionConectada["nombre"].asString()
                    UbicacionResumida(id, nombre)
                }.size
        }
    }

    override fun clear() {
        return driver.session().use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
    }
}