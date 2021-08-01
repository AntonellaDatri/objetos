package ar.edu.unq.eperdemic.utils.jdbc

import ar.edu.unq.eperdemic.persistencia.dao.DataDAO
import ar.edu.unq.eperdemic.utils.DataService


class DataServiceJDBC(var dataDao: DataDAO) : DataService {
    override fun crearSetDeDatosIniciales() {
       dataDao.crearSetDeDatosIniciales()
    }

    override fun eliminarTodo() {
        dataDao.clear()
    }
}