package com.example.prstamolabctma

import com.example.prstamolabctma.data.repository.InMemoryPrestamoRepository
import com.example.prstamolabctma.model.CategoriaEquipo
import com.example.prstamolabctma.model.Equipo
import com.example.prstamolabctma.model.EstadoEquipo
import com.example.prstamolabctma.model.EstadoSolicitud
import com.example.prstamolabctma.model.SolicitudPrestamo
import com.example.prstamolabctma.viewmodel.PrestamoViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PrestamoViewModelTest {

    private lateinit var repository: InMemoryPrestamoRepository
    private lateinit var viewModel: PrestamoViewModel

    @Before
    fun setUp() {
        repository = InMemoryPrestamoRepository()
        viewModel = PrestamoViewModel(repository)
    }

    // =========================================================================
    // 1. VISUALIZACIÓN, CATÁLOGO Y EQUIPOS DISPONIBLES
    // =========================================================================

    @Test
    fun test_01_verificar_que_se_muestren_los_equipos_disponibles() {
        val equipos = viewModel.uiState.value.equipos
        val disponibles = equipos.filter { it.estado == EstadoEquipo.DISPONIBLE }

        assertFalse("Debe haber equipos cargados", equipos.isEmpty())
        assertTrue("Los equipos disponibles deben mostrarse", disponibles.isNotEmpty())
    }

    @Test
    fun test_02_verificar_que_cada_equipo_aparezca_de_manera_individual() {
        val equipos = viewModel.uiState.value.equipos
        val idsUnicos = equipos.map { it.id }.toSet()

        assertEquals("Cada equipo debe ser único y distinguible por ID", equipos.size, idsUnicos.size)
    }

    @Test
    fun test_03_verificar_que_la_informacion_de_cada_equipo_sea_correcta() {
        val equipo = repository.obtenerEquipo(1)

        assertNotNull("El equipo debe existir en el repositorio", equipo)
        assertEquals("Multímetro", equipo?.nombre)
        assertEquals(CategoriaEquipo.ELECTRONICA, equipo?.categoria)
        assertEquals(EstadoEquipo.DISPONIBLE, equipo?.estado)
    }

    @Test
    fun test_04_verificar_que_los_equipos_no_disponibles_no_aparezcan_como_disponibles() {
        // Reservamos un equipo en el repositorio
        val solicitud = SolicitudPrestamo(1, 1, "Lab 1", "Práctica de prueba", 2, EstadoSolicitud.SOLICITADA)
        repository.crearSolicitud(solicitud)
        viewModel.cargarEquipos()

        val equipoReservado = viewModel.uiState.value.equipos.find { it.id == 1 }
        assertNotNull(equipoReservado)
        assertNotEquals("Un equipo reservado no debe figurar como DISPONIBLE", EstadoEquipo.DISPONIBLE, equipoReservado?.estado)
    }

    @Test
    fun test_05_verificar_que_se_pueda_visualizar_correctamente_la_informacion_de_cada_equipo() {
        val equipo = viewModel.uiState.value.equipos.find { it.id == 2 }

        assertNotNull(equipo)
        assertEquals("Cámara Canon", equipo?.nombre)
        assertEquals(CategoriaEquipo.CAMARA, equipo?.categoria)
    }

    @Test
    fun test_06_verificar_que_se_muestre_el_catalogo() {
        val catalogo = viewModel.uiState.value.equipos
        assertNotNull("El catálogo no debe ser nulo", catalogo)
        assertTrue("El catálogo debe contener elementos", catalogo.size >= 3)
    }

    @Test
    fun test_07_verificar_que_cada_equipo_muestre_nombre_categoria_y_estado() {
        val equipo = viewModel.uiState.value.equipos.first()

        assertNotNull("Nombre obligatorio", equipo.nombre)
        assertNotNull("Categoría obligatoria", equipo.categoria)
        assertNotNull("Estado obligatorio", equipo.estado)
    }

    @Test
    fun test_08_verificar_que_se_muestren_varios_equipos_correctamente() {
        val equipos = viewModel.uiState.value.equipos
        assertTrue("Debe renderizarse una lista de varios equipos", equipos.size > 1)
    }

    @Test
    fun test_09_verificar_el_comportamiento_cuando_no_hay_equipos_registrados() {
        val repoVacio = object : com.example.prstamolabctma.data.repository.PrestamoRepository {
            override fun obtenerEquipos(): List<Equipo> = emptyList()
            override fun obtenerEquipo(id: Int): Equipo? = null
            override fun obtenerSolicitudes(): List<SolicitudPrestamo> = emptyList()
            override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = null
            override fun crearSolicitud(solicitud: SolicitudPrestamo): Boolean = false
            override fun cancelarSolicitud(id: Int): Boolean = false
        }
        val vmVacio = PrestamoViewModel(repoVacio)

        assertTrue("La lista de equipos debe estar vacía sin lanzar excepciones", vmVacio.uiState.value.equipos.isEmpty())
    }

    // =========================================================================
    // 2. SELECCIÓN DE EQUIPO E INFORMACIÓN
    // =========================================================================

    @Test
    fun test_10_verificar_que_se_pueda_seleccionar_un_equipo() {
        val equipo = repository.obtenerEquipo(1)
        assertNotNull("Se puede seleccionar un equipo válido", equipo)
    }

    @Test
    fun test_11_verificar_que_un_equipo_valido_muestre_su_informacion() {
        val equipo = repository.obtenerEquipo(3)
        assertNotNull(equipo)
        assertEquals("Tablet Samsung", equipo?.nombre)
    }

    @Test
    fun test_12_verificar_que_los_datos_del_equipo_sean_correctos() {
        val equipo = repository.obtenerEquipo(2)
        assertEquals(2, equipo?.id)
        assertEquals("Cámara Canon", equipo?.nombre)
        assertEquals(CategoriaEquipo.CAMARA, equipo?.categoria)
    }

    @Test
    fun test_13_verificar_el_comportamiento_al_seleccionar_un_equipo_invalido() {
        val equipoInvalido = repository.obtenerEquipo(999)
        assertNull("La búsqueda de un ID inexistente debe devolver null", equipoInvalido)
    }

    // =========================================================================
    // 3. VALIDACIÓN DE CAMPOS Y SOLICITUDES INVÁLIDAS
    // =========================================================================

    @Test
    fun test_14_verificar_proposito_con_menos_de_10_caracteres() {
        viewModel.crearSolicitud(1, "Lab 101", "Corto", "2")

        val state = viewModel.uiState.value
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", state.mensaje)
        assertFalse("Guardando debe quedar desbloqueado (false)", state.guardando)
        assertTrue("No se debe crear la solicitud", state.solicitudes.isEmpty())
    }

    @Test
    fun test_15_verificar_proposito_entre_10_y_180_caracteres() {
        viewModel.crearSolicitud(1, "Lab 101", "Esta es una descripción válida de propósito.", "2")

        val state = viewModel.uiState.value
        assertEquals("Solicitud registrada con éxito.", state.mensaje)
        assertEquals(1, state.solicitudes.size)
    }

    @Test
    fun test_16_verificar_proposito_mayor_a_180_caracteres() {
        val propositoExcesivo = "A".repeat(181)
        viewModel.crearSolicitud(1, "Lab 101", propositoExcesivo, "2")

        val state = viewModel.uiState.value
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", state.mensaje)
        assertFalse(state.guardando)
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun test_17_verificar_duracion_entre_1_y_8_horas_menor_a_1() {
        viewModel.crearSolicitud(1, "Lab 101", "Práctica de desarrollo de software", "0")

        val state = viewModel.uiState.value
        assertEquals("La duración debe estar entre 1 y 8 horas.", state.mensaje)
        assertFalse(state.guardando)
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun test_18_verificar_duracion_entre_1_y_8_horas_mayor_a_8() {
        viewModel.crearSolicitud(1, "Lab 101", "Práctica de desarrollo de software", "9")

        val state = viewModel.uiState.value
        assertEquals("La duración debe estar entre 1 y 8 horas.", state.mensaje)
        assertFalse(state.guardando)
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun test_19_verificar_que_el_destino_sea_obligatorio() {
        viewModel.crearSolicitud(1, "", "Práctica de desarrollo de software", "2")

        val state = viewModel.uiState.value
        assertEquals("El destino es obligatorio.", state.mensaje)
        assertFalse(state.guardando)
        assertTrue(state.solicitudes.isEmpty())
    }

    @Test
    fun test_20_verificar_que_no_se_guarde_una_solicitud_con_datos_invalidos() {
        viewModel.crearSolicitud(1, "", "", "ABC")

        val state = viewModel.uiState.value
        assertTrue("La lista de solicitudes debe mantenerse vacía", state.solicitudes.isEmpty())
        assertFalse("El estado guardando debe reiniciarse a false", state.guardando)
    }

    // =========================================================================
    // 4. REGISTRO, PREVENCIÓN DE DUPLICADOS Y MANEJO DE ESTADOS (FIXES)
    // =========================================================================

    @Test
    fun test_21_verificar_que_se_pueda_registrar_una_solicitud_valida() {
        viewModel.crearSolicitud(1, "Ambiente 204", "Práctica de laboratorio de prueba", "3")

        val state = viewModel.uiState.value
        assertEquals(1, state.solicitudes.size)
        assertEquals("Solicitud registrada con éxito.", state.mensaje)
    }

    @Test
    fun test_22_verificar_que_la_solicitud_quede_en_estado_SOLICITADA() {
        viewModel.crearSolicitud(1, "Ambiente 204", "Práctica de laboratorio de prueba", "3")

        val solicitud = viewModel.uiState.value.solicitudes.first()
        assertEquals(EstadoSolicitud.SOLICITADA, solicitud.estado)
    }

    @Test
    fun test_23_verificar_que_el_equipo_pase_a_RESERVADO() {
        viewModel.crearSolicitud(1, "Ambiente 204", "Práctica de laboratorio de prueba", "3")

        val equipo = viewModel.uiState.value.equipos.find { it.id == 1 }
        assertEquals(EstadoEquipo.RESERVADO, equipo?.estado)
    }

    @Test
    fun test_24_verificar_que_una_pulsacion_en_guardar_cree_una_sola_solicitud() {
        viewModel.crearSolicitud(1, "Aula 101", "Práctica de desarrollo móvil", "2")

        val state = viewModel.uiState.value
        assertEquals(1, state.solicitudes.size)
        assertFalse(state.guardando)
    }

    @Test
    fun test_25_verificar_que_una_doble_pulsacion_rapida_en_guardar_no_cree_solicitudes_duplicadas() {
        // Primera ejecución habilita guardando
        viewModel.crearSolicitud(1, "Aula 101", "Práctica de desarrollo móvil", "2")

        // Segunda ejecución inmediata al intentar enviar dos veces
        viewModel.crearSolicitud(1, "Aula 101", "Práctica de desarrollo móvil", "2")

        val state = viewModel.uiState.value
        assertEquals("Debe existir únicamente 1 solicitud registrada", 1, state.solicitudes.size)
        assertEquals("El equipo seleccionado no está disponible.", state.mensaje)
    }

    @Test
    fun test_26_verificar_que_despues_de_guardar_solamente_exista_una_solicitud() {
        viewModel.crearSolicitud(1, "Aula 101", "Práctica de desarrollo móvil", "2")

        assertEquals(1, viewModel.uiState.value.solicitudes.size)
    }

    @Test
    fun test_27_verificar_que_la_informacion_de_la_solicitud_se_mantenga_correctamente() {
        viewModel.crearSolicitud(2, "Laboratorio 3", "Mediciones con cámara profesional", "5")

        val solicitud = viewModel.uiState.value.solicitudes.first()
        assertEquals(2, solicitud.equipoId)
        assertEquals("Laboratorio 3", solicitud.ambienteDestino)
        assertEquals("Mediciones con cámara profesional", solicitud.proposito)
        assertEquals(5, solicitud.duracionHoras)
    }

    // =========================================================================
    // 5. CANCELACIÓN DE SOLICITUDES
    // =========================================================================

    @Test
    fun test_28_verificar_que_se_pueda_cancelar_una_solicitud_solicitada_pase_a_CANCELADA_y_equipo_a_DISPONIBLE() {
        // 1. Crear solicitud
        viewModel.crearSolicitud(3, "Sala A", "Práctica con Tablet en aula", "4")
        val solicitudId = viewModel.uiState.value.solicitudes.first().id

        // 2. Cancelar solicitud
        viewModel.cancelarSolicitud(solicitudId)

        val state = viewModel.uiState.value
        val solicitudCancelada = state.solicitudes.find { it.id == solicitudId }
        val equipoLiberado = state.equipos.find { it.id == 3 }

        assertEquals("La solicitud debe pasar a CANCELADA", EstadoSolicitud.CANCELADA, solicitudCancelada?.estado)
        assertEquals("El equipo debe volver a DISPONIBLE", EstadoEquipo.DISPONIBLE, equipoLiberado?.estado)
    }
}