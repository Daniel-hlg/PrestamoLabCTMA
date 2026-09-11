package com.example.prestamolabctma.data

import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.EstadoSolicitud
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class InMemoryPrestamoRepositoryTest {

    private lateinit var repository: InMemoryPrestamoRepository

    @Before
    fun setUp() {
        repository = InMemoryPrestamoRepository()
    }

    // --- GRUPO 1: Catálogo y Equipos disponibles ---

    @Test
    fun testVerificarQueSeMuestrenLosEquiposDisponibles() {
        val equipos = repository.listarEquipos()
        val disponibles = equipos.filter { it.estado == EstadoEquipo.DISPONIBLE }
        assertFalse("Debe haber equipos disponibles en el catálogo", disponibles.isEmpty())
    }

    @Test
    fun testVerificarQueCadaEquipoAparezcaDeManeraIndividual() {
        val equipos = repository.listarEquipos()
        val ids = equipos.map { it.id }
        assertEquals("Cada equipo debe ser individual con ID único", ids.size, ids.distinct().size)
    }

    @Test
    fun testVerificarQueLaInformacionDeCadaEquipoSeaCorrecta() {
        val equipo = repository.obtenerEquipo(1)
        assertNotNull(equipo)
        assertEquals("Kit Arduino UNO", equipo?.nombre)
        assertNotNull(equipo?.categoria)
        assertNotNull(equipo?.descripcion)
    }

    @Test
    fun testVerificarQueLosEquiposNoDisponiblesNoAparezcanComoDisponibles() {
        val equipos = repository.listarEquipos()
        val proyector = equipos.find { it.id == 4 } // Proyector Epson está RESERVADO inicialmente
        assertNotNull(proyector)
        assertNotEquals(EstadoEquipo.DISPONIBLE, proyector?.estado)
    }

    @Test
    fun testVerificarQueSePuedaVisualizarCorrectamenteLaInformacionDeCadaEquipo() {
        val equipos = repository.listarEquipos()
        for (eq in equipos) {
            val encontrado = repository.obtenerEquipo(eq.id)
            assertNotNull(encontrado)
            assertEquals(eq.nombre, encontrado?.nombre)
            assertEquals(eq.estado, encontrado?.estado)
        }
    }

    // --- GRUPO 2: Registro de solicitud válida y estados ---

    @Test
    fun testVerificarQueSePuedaRegistrarUnaSolicitudValida() {
        // El equipo 1 está DISPONIBLE
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        )
        assertTrue(resultado.isSuccess)
        assertNotNull(resultado.getOrNull())
    }

    @Test
    fun testVerificarQueLaSolicitudQuedeEnEstadoSOLICITADA() {
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        )
        val solicitud = resultado.getOrNull()
        assertNotNull(solicitud)
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud?.estado)
    }

    @Test
    fun testVerificarQueElEquipoPaseARESERVADO() {
        val equipoAntes = repository.obtenerEquipo(1)
        assertEquals(EstadoEquipo.DISPONIBLE, equipoAntes?.estado)

        repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        )

        val equipoDespues = repository.obtenerEquipo(1)
        assertEquals(EstadoEquipo.RESERVADO, equipoDespues?.estado)
    }

    @Test
    fun testVerificarQueNoSeCreanSolicitudesDuplicadasParaMismoEquipoActivo() {
        val resultado1 = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        )
        assertTrue(resultado1.isSuccess)

        // Intentar crear otra solicitud para el mismo equipo que ahora está RESERVADO/activo
        val resultado2 = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio B",
            proposito = "Práctica de robótica avanzada con Arduino",
            duracionHoras = 2
        )
        assertTrue(resultado2.isFailure)
        assertEquals("El equipo no está disponible.", resultado2.exceptionOrNull()?.message)
    }

    // --- GRUPO 3: Cancelación ---

    @Test
    fun testVerificarQueSePuedaCancelarUnaSolicitudSOLICITADA() {
        val resultadoSol = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        ).getOrThrow()

        val resultadoCancel = repository.cancelarSolicitud(resultadoSol.id)
        assertTrue(resultadoCancel.isSuccess)
    }

    @Test
    fun testVerificarQueLaSolicitudPaseACANCELADA() {
        val resultadoSol = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        ).getOrThrow()

        repository.cancelarSolicitud(resultadoSol.id)
        val solicitudAct = repository.obtenerSolicitud(resultadoSol.id)
        assertEquals(EstadoSolicitud.CANCELADA, solicitudAct?.estado)
    }

    @Test
    fun testVerificarQueElEquipoPaseADISPONIBLE() {
        val resultadoSol = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Práctica de robótica básica con Arduino",
            duracionHoras = 4
        ).getOrThrow()

        assertEquals(EstadoEquipo.RESERVADO, repository.obtenerEquipo(1)?.estado)

        repository.cancelarSolicitud(resultadoSol.id)
        assertEquals(EstadoEquipo.DISPONIBLE, repository.obtenerEquipo(1)?.estado)
    }

    // --- GRUPO 4: Validaciones de Datos ---

    @Test
    fun testVerificarPropositoConMenosDe10Caracteres() {
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Corto", // 5 caracteres
            duracionHoras = 4
        )
        assertTrue(resultado.isFailure)
        assertTrue(resultado.exceptionOrNull() is IllegalArgumentException)
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun testVerificarPropositoEntre10Y180Caracteres() {
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "Laboratorio A",
            proposito = "Cumple con el tamaño mínimo requerido", // >10 y <180
            duracionHoras = 4
        )
        assertTrue(resultado.isSuccess)
    }

    @Test
    fun testVerificarDuracionEntre1Y8Horas() {
        // Límite inferior válido (1 hora)
        assertTrue(repository.crearSolicitud(1, "Lab A", "Proposito válido largo", 1).isSuccess)
        
        // Límite superior válido (8 horas)
        // Usamos equipo 2 ya que el 1 pasó a reservado en la línea anterior
        assertTrue(repository.crearSolicitud(2, "Lab A", "Proposito válido largo", 8).isSuccess)

        // Inválido por debajo (0 horas)
        val resBajo = repository.crearSolicitud(3, "Lab A", "Proposito válido largo", 0)
        assertTrue(resBajo.isFailure)
        assertEquals("La duración debe estar entre 1 y 8 horas.", resBajo.exceptionOrNull()?.message)

        // Inválido por encima (9 horas)
        val resAlto = repository.crearSolicitud(3, "Lab A", "Proposito válido largo", 9)
        assertTrue(resAlto.isFailure)
        assertEquals("La duración debe estar entre 1 y 8 horas.", resAlto.exceptionOrNull()?.message)
    }

    @Test
    fun testVerificarQueElDestinoSeaObligatorio() {
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "   ", // Vacío / espacios
            proposito = "Proposito válido largo",
            duracionHoras = 4
        )
        assertTrue(resultado.isFailure)
        assertEquals("El ambiente destino es obligatorio.", resultado.exceptionOrNull()?.message)
    }

    @Test
    fun testVerificarQueNoSeGuardeUnaSolicitudConDatosInvalidos() {
        val inicialCount = repository.listarSolicitudes().size
        
        val resultado = repository.crearSolicitud(
            equipoId = 1,
            ambienteDestino = "",
            proposito = "Invalido",
            duracionHoras = 24
        )
        
        assertTrue(resultado.isFailure)
        assertEquals(inicialCount, repository.listarSolicitudes().size)
    }

    // --- GRUPO 5: Selección de equipo e información ---

    @Test
    fun testVerificarQueSePuedaSeleccionarUnEquipo() {
        val equipo = repository.obtenerEquipo(2)
        assertNotNull(equipo)
    }

    @Test
    fun testVerificarQueUnEquipoValidoMuestreSuInformacion() {
        val equipo = repository.obtenerEquipo(2)
        assertEquals("Portátil Lenovo", equipo?.nombre)
        assertEquals(com.example.prestamolabctma.model.CategoriaEquipo.COMPUTO, equipo?.categoria)
    }

    @Test
    fun testVerificarElComportamientoAlSeleccionarUnEquipoInvalido() {
        val equipo = repository.obtenerEquipo(999) // ID inexistente
        assertNull(equipo)
    }
}
