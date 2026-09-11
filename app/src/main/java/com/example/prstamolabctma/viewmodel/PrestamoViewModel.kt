package com.example.prstamolabctma.viewmodel

import androidx.lifecycle.ViewModel
import com.example.prstamolabctma.data.repository.PrestamoRepository
import com.example.prstamolabctma.model.EstadoEquipo
import com.example.prstamolabctma.model.EstadoSolicitud
import com.example.prstamolabctma.model.SolicitudPrestamo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PrestamoViewModel(private val repository: PrestamoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PrestamoUiState())
    val uiState: StateFlow<PrestamoUiState> = _uiState

    init {
        cargarEquipos()
        actualizarSolicitudes()
    }

    fun cargarEquipos() {
        _uiState.value = _uiState.value.copy(
            equipos = repository.obtenerEquipos()
        )
    }

    private fun actualizarSolicitudes() {
        _uiState.value = _uiState.value.copy(
            solicitudes = repository.obtenerSolicitudes()
        )
    }

    fun crearSolicitud(equipoId: Int, destino: String, proposito: String, horasTexto: String) {

        // 0. Evitar reentradas mientras procesa
        if (_uiState.value.guardando) {
            return
        }
        _uiState.value = _uiState.value.copy(guardando = true)

        // 1. Validar Destino
        if (destino.isBlank()) {
            _uiState.value = _uiState.value.copy(
                mensaje = "El destino es obligatorio.",
                guardando = false
            )
            return
        }

        // 2. Validar Propósito
        val propositoLimpio = proposito.trim()
        if (propositoLimpio.length < 10 || propositoLimpio.length > 180) {
            _uiState.value = _uiState.value.copy(
                mensaje = "El propósito debe tener entre 10 y 180 caracteres.",
                guardando = false
            )
            return
        }

        // 3. Validar Duración
        val horas = horasTexto.toIntOrNull()
        if (horas == null || horas !in 1..8) {
            _uiState.value = _uiState.value.copy(
                mensaje = "La duración debe estar entre 1 y 8 horas.",
                guardando = false
            )
            return
        }

        // 4. Verificar disponibilidad previa en el repositorio
        val equipo = repository.obtenerEquipo(equipoId)
        if (equipo == null || equipo.estado != EstadoEquipo.DISPONIBLE) {
            _uiState.value = _uiState.value.copy(
                mensaje = "El equipo seleccionado no está disponible.",
                guardando = false
            )
            return
        }

        // 5. Intentar guardar mediante el repositorio
        val nuevaSolicitud = SolicitudPrestamo(
            id = repository.obtenerSolicitudes().size + 1,
            equipoId = equipoId,
            ambienteDestino = destino,
            proposito = propositoLimpio,
            duracionHoras = horas,
            estado = EstadoSolicitud.SOLICITADA
        )

        val guardadoExitoso = repository.crearSolicitud(nuevaSolicitud)

        if (guardadoExitoso) {
            _uiState.value = _uiState.value.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes(),
                mensaje = "Solicitud registrada con éxito.",
                guardando = false
            )
        } else {
            _uiState.value = _uiState.value.copy(
                mensaje = "No se pudo registrar la solicitud.",
                guardando = false
            )
        }
    }

    fun cancelarSolicitud(id: Int) {
        val cancelado = repository.cancelarSolicitud(id)
        if (cancelado) {
            _uiState.value = _uiState.value.copy(
                equipos = repository.obtenerEquipos(),
                solicitudes = repository.obtenerSolicitudes(),
                mensaje = "Solicitud cancelada con éxito."
            )
        }
    }
}