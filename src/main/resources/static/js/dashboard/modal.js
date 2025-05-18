/**
 * Funciones JavaScript para los modales del dashboard
 */

// Inicialización del modal de cancelación
document.addEventListener('DOMContentLoaded', function() {
    setupCancelationModal();
});

/**
 * Configura el modal de cancelación de reservas
 */
function setupCancelationModal() {
    const modal = document.getElementById('cancelReservationModal');
    if (!modal) return;

    const confirmBtn = document.getElementById('confirmCancelBtn');
    const cancelBtn = document.getElementById('cancelModalBtn');
    const reservationDetails = document.getElementById('reservationDetails');

    // Funcionalidad para los botones de cancelar reserva
    document.querySelectorAll('.cancel-reservation').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const reservationId = this.getAttribute('data-reservation-id');
            const reservationDate = this.getAttribute('data-reservation-date');

            // Actualizar el enlace de confirmación con el ID de la reserva
            confirmBtn.href = `/reservations/cancel/${reservationId}`;

            // Mostrar detalles de la reserva
            reservationDetails.innerHTML = `<strong>Fecha:</strong> ${reservationDate}`;

            // Mostrar el modal
            modal.classList.add('active');
        });
    });

    // Cerrar el modal al hacer clic en "Cancelar"
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function() {
            modal.classList.remove('active');
        });
    }

    // Cerrar el modal al hacer clic fuera de él
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.classList.remove('active');
        }
    });
}