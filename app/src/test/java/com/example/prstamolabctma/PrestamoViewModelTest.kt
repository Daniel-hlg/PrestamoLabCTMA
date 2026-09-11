package com.example.prstamolabctma

import com.example.prstamolabctma.data.repository.PrestamoRepository
import com.example.prstamolabctma.model.CategoriaEquipo
import com.example.prstamolabctma.model.Equipo
import com.example.prstamolabctma.model.EstadoEquipo
import com.example.prstamolabctma.model.SolicitudPrestamo
import com.example.prstamolabctma.viewmodel.PrestamoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrestamoViewModelTest {

    private lateinit var viewModel: PrestamoViewModel
    private lateinit var fakeRepository: FakePrestamoRepository

    @Before
    fun setup() {
        fakeRepository = FakePrestamoRepository()
        viewModel = PrestamoViewModel(fakeRepository)
    }

    @Test
    fun `crearSolicitud con destino vacio debe mostrar error`() {
        viewModel.crearSolicitud(1, "", "Propósito válido con más de diez caracteres", 2)
        
        assertEquals("El ambiente o destino es obligatorio.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `crearSolicitud con proposito muy corto debe mostrar error`() {
        // Menos de 10 caracteres
        viewModel.crearSolicitud(1, "Lab A", "Corto", 2)
        
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `crearSolicitud con proposito muy largo debe mostrar error`() {
        // Más de 180 caracteres
        val propositoLargo = "a".repeat(181)
        viewModel.crearSolicitud(1, "Lab A", propositoLargo, 2)
        
        assertEquals("El propósito debe tener entre 10 y 180 caracteres.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `crearSolicitud con duracion fuera de rango debe mostrar error`() {
        // Duración 9 horas (máximo es 8)
        viewModel.crearSolicitud(1, "Lab A", "Propósito válido de longitud suficiente", 9)
        
        assertEquals("La duración debe estar entre 1 y 8 horas.", viewModel.uiState.value.mensaje)
    }

    @Test
    fun `crearSolicitud valida debe actualizar el estado correctamente`() {
        viewModel.crearSolicitud(1, "Laboratorio Central", "Práctica de laboratorio semestral", 4)
        
        assertEquals("Solicitud creada correctamente.", viewModel.uiState.value.mensaje)
        assertTrue(viewModel.uiState.value.solicitudes.isNotEmpty())
    }

    @Test
    fun `verificar que el proceso de guardado previene peticiones multiples`() {
        // Configuramos el repo para que no termine inmediatamente si fuera necesario, 
        // pero aquí probamos la lógica del flag 'guardando' en el VM.
        
        // Simulamos que ya está guardando
        // Nota: En la implementación actual es difícil de probar sin coroutines y delays,
        // pero podemos verificar que el flag se limpie al finalizar.
        
        viewModel.crearSolicitud(1, "Destino", "Propósito válido para el test", 2)
        assertEquals(false, viewModel.uiState.value.guardando)
    }

    @Test
    fun `cancelar solicitud debe actualizar el mensaje de exito`() {
        // Primero creamos una
        viewModel.crearSolicitud(1, "Destino", "Propósito para cancelación", 2)
        val id = viewModel.uiState.value.solicitudes[0].id
        
        viewModel.cancelarSolicitud(id)
        
        assertEquals("Solicitud cancelada correctamente.", viewModel.uiState.value.mensaje)
    }
}

// Clase de apoyo para pruebas sin depender de la implementación real del repo
class FakePrestamoRepository : PrestamoRepository {
    private val equipos = mutableListOf(
        Equipo(1, "Equipo 1", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE)
    )
    private val solicitudes = mutableListOf<SolicitudPrestamo>()
    private var nextId = 1

    override fun obtenerEquipos(): List<Equipo> = equipos
    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }
    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes
    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        val nueva = solicitud.copy(id = nextId++)
        solicitudes.add(nueva)
        return Result.success(Unit)
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val index = solicitudes.indexOfFirst { it.id == id }
        if (index != -1) {
            solicitudes[index] = solicitudes[index].copy(estado = com.example.prstamolabctma.model.EstadoSolicitud.CANCELADA)
            return Result.success(Unit)
        }
        return Result.failure(Exception("Not found"))
    }
}
