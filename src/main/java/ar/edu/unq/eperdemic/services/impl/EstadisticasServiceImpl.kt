package ar.edu.unq.eperdemic.services.impl

import ar.edu.unq.eperdemic.modelo.Especie
import ar.edu.unq.eperdemic.modelo.ReporteDeContagios
import ar.edu.unq.eperdemic.modelo.TipoDeVector
import ar.edu.unq.eperdemic.modelo.Vector
import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.persistencia.dao.EspecieDAO
import ar.edu.unq.eperdemic.persistencia.dao.UbicacionDAO
import ar.edu.unq.eperdemic.services.EstadisticasService
import ar.edu.unq.eperdemic.services.runner.TransactionRunner
import ar.edu.unq.eperdemic.services.runner.TransactionRunner.runTrx
import kotlin.math.absoluteValue

class EstadisticasServiceImpl (private val especieDAO: EspecieDAO, private val dataDAO : DataDAO, private val ubicacionDAO: UbicacionDAO) : EstadisticasService {

    override fun especieLider(): Especie {
       return runTrx {
           especieDAO.lider()
       }
    }

    override fun lideres(): List<Especie> {
        return runTrx {
            especieDAO.lideres()
        }
    }

    override fun reporteDeContagios(ubicacionId: Long): ReporteDeContagios {
       return runTrx {
           val cantPresente = ubicacionDAO.cantidadDeVectoresPresentes(ubicacionId)
           val cantInfectados = ubicacionDAO.cantidadDeVectoresInfectados(ubicacionId)
           val EspecieConMasContagios = ubicacionDAO.especieQueInfectoAMasVectores(ubicacionId)
           ReporteDeContagios(cantPresente,cantInfectados,EspecieConMasContagios!!)
       }
    }
}