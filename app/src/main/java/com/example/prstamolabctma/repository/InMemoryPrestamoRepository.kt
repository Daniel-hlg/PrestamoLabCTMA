package com.example.prstamolabctma.repository
import com.example.prstamolabctma.model.*

class InMemoryPrestamoRepository : PrestamoRepository {
    private val equipos = mutableListOf(
        Equipo(1, "Multímetro", CategoriaEquipo.ELECTRONICA, EstadoEquipo.DISPONIBLE),
        Equipo(2, "Laptop", CategoriaEquipo.INFORMATICA, EstadoEquipo.DISPONIBLE),
        Equipo(3, "Cámara", CategoriaEquipo.OTRO, EstadoEquipo.RESERVADO)
    )

    private val solicitudes = mutableListOf<SolicitudPrestamo>()

    override fun obtenerEquipos(): List<Equipo> = equipos
    override fun obtenerEquipo(id: Int): Equipo? = equipos.find { it.id == id }
    override fun obtenerSolicitudes(): List<SolicitudPrestamo> = solicitudes
    override fun obtenerSolicitud(id: Int): SolicitudPrestamo? = solicitudes.find { it.id == id }

    override fun crearSolicitud(solicitud: SolicitudPrestamo): Result<Unit> {
        // Validaciones de negocio
        if (solicitud.ambienteDestino.isBlank()) {
            return Result.failure(Exception("El destino es obligatorio"))
        }
        if (solicitud.proposito.length < 10 || solicitud.proposito.length > 180) {
            return Result.failure(Exception("El propósito debe tener entre 10 y 180 caracteres"))
        }
        if (solicitud.duracionHoras < 1 || solicitud.duracionHoras > 8) {
            return Result.failure(Exception("La duración debe estar entre 1 y 8 horas"))
        }

        // Verificar si ya existe una solicitud activa para este equipo
        val solicitudDuplicada = solicitudes.any { it.equipoId == solicitud.equipoId && it.estado == EstadoSolicitud.SOLICITADA }
        if (solicitudDuplicada) {
            return Result.failure(Exception("Ya existe una solicitud activa para este equipo"))
        }

        val equipo = obtenerEquipo(solicitud.equipoId)
        return if (equipo != null && equipo.estado == EstadoEquipo.DISPONIBLE) {
            solicitudes.add(solicitud)
            equipo.estado = EstadoEquipo.RESERVADO
            Result.success(Unit)
        } else {
            Result.failure(Exception("Equipo no disponible"))
        }
    }

    override fun cancelarSolicitud(id: Int): Result<Unit> {
        val solicitud = obtenerSolicitud(id)
        return if (solicitud != null && solicitud.estado == EstadoSolicitud.SOLICITADA) {
            solicitud.estado = EstadoSolicitud.CANCELADA
            var equipo = obtenerEquipo(solicitud.equipoId)
            equipo?.estado = EstadoEquipo.DISPONIBLE
            Result.success(Unit)
        } else {
            Result.failure(Exception("No se puede cancelar"))
        }
    }
}
