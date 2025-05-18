/**
 * Funciones JavaScript para el calendario del dashboard
 */

// Variables globales
let currentDate = new Date();
let currentYear = currentDate.getFullYear();
let currentMonth = currentDate.getMonth();

// Inicialización del calendario
document.addEventListener('DOMContentLoaded', function() {
    if (document.querySelector('.calendar-grid')) {
        setupUserReservationsFromThymeleaf();
        setupCalendarNavigation();
    }
});

/**
 * Configura la navegación del calendario
 */
function setupCalendarNavigation() {
    // Botones de navegación
    const prevButton = document.querySelector('.calendar-header button:first-child');
    const nextButton = document.querySelector('.calendar-header button:last-child');

    if (!prevButton || !nextButton) return;

    // Navegar al mes anterior
    prevButton.addEventListener('click', function() {
        currentMonth--;
        if (currentMonth < 0) {
            currentMonth = 11;
            currentYear--;
        }
        generateCalendar(currentYear, currentMonth);
    });

    // Navegar al mes siguiente
    nextButton.addEventListener('click', function() {
        currentMonth++;
        if (currentMonth > 11) {
            currentMonth = 0;
            currentYear++;
        }
        generateCalendar(currentYear, currentMonth);
    });

    // Generar calendario inicial
    generateCalendar(currentYear, currentMonth);
}

/**
 * Genera el calendario para el mes y año especificados
 */
function generateCalendar(year, month) {
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const totalDays = lastDay.getDate();

    // Ajuste para que la semana comience en lunes (0: domingo, 1: lunes, ..., 6: sábado)
    let startDay = firstDay.getDay() - 1;
    if (startDay === -1) startDay = 6; // Si es domingo (0), lo convertimos a 6

    const calendarGrid = document.querySelector('.calendar-grid');
    const monthYearDisplay = document.querySelector('.calendar-header h3');

    // Actualizar el encabezado del mes
    const monthNames = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    monthYearDisplay.textContent = `${monthNames[month]} ${year}`;

    // Obtener los encabezados de días de la semana
    const dayHeaders = [];
    const existingHeaders = document.querySelectorAll('.calendar-day-header');
    existingHeaders.forEach(header => {
        dayHeaders.push(header.cloneNode(true));
    });

    // Limpiar la cuadrícula del calendario
    calendarGrid.innerHTML = '';

    // Agregar los encabezados de días
    dayHeaders.forEach(header => {
        calendarGrid.appendChild(header);
    });

    // Agregar espacios en blanco para el principio del mes
    for (let i = 0; i < startDay; i++) {
        const emptyDay = document.createElement('div');
        emptyDay.className = 'calendar-day empty';
        calendarGrid.appendChild(emptyDay);
    }

    // Agregar los días del mes
    const today = new Date();
    const currentDay = today.getDate();
    const currentMonth = today.getMonth();
    const currentYear = today.getFullYear();

    // Obtener reservas del usuario (desde una variable global definida en Thymeleaf)
    const userReservations = window.userReservations || [];

    for (let i = 1; i <= totalDays; i++) {
        const dayElement = document.createElement('div');
        const isToday = currentDay === i && currentMonth === month && currentYear === year;
        const isPast = new Date(year, month, i) < new Date(currentYear, currentMonth, currentDay);

        // Verificar si hay reservas para este día
        const hasReservation = userReservations.some(reservation => {
            const reservationDate = new Date(reservation.date);
            return reservationDate.getDate() === i &&
                reservationDate.getMonth() === month &&
                reservationDate.getFullYear() === year;
        });

        // Asignar clases según condiciones
        dayElement.className = 'calendar-day';
        if (isToday) dayElement.classList.add('today');
        if (hasReservation) dayElement.classList.add('has-reservation');
        if (isPast) dayElement.classList.add('past');

        dayElement.textContent = i;

        // Si hay una reserva, agregar un indicador visual
        if (hasReservation) {
            const indicator = document.createElement('div');
            indicator.className = 'reservation-indicator';
            dayElement.appendChild(indicator);
        }

        // Añadir evento de clic para días futuros o hoy
        if (!isPast || isToday) {
            dayElement.classList.add('clickable');
            dayElement.addEventListener('click', function() {
                showTimeSelector(i, month, year);
            });
        }

        calendarGrid.appendChild(dayElement);
    }
}

/**
 * Inicializa los datos de reservas del usuario
 */
function setupUserReservationsFromThymeleaf() {
    // Inicializar el array de reservas
    if (!window.userReservations || window.userReservations.length === 0) {
        console.info("No se encontraron reservas en el contexto, usando datos de ejemplo");

        // Si no hay reservas (probablemente en entorno de desarrollo), usar datos de ejemplo
        const today = new Date();
        const tomorrow = new Date();
        tomorrow.setDate(today.getDate() + 1);

        window.userReservations = [
            { date: today.toISOString(), id: 1 },
            { date: tomorrow.toISOString(), id: 2 }
        ];
    }
}

/**
 * Muestra el selector de hora para una fecha específica
 */
function showTimeSelector(day, month, year) {
    const dateSelected = new Date(year, month, day);
    const today = new Date();

    // Si la fecha es anterior a hoy, no permitir reservas
    if (dateSelected < today && !(dateSelected.getDate() === today.getDate() &&
        dateSelected.getMonth() === today.getMonth() &&
        dateSelected.getFullYear() === today.getFullYear())) {
        alert('No puedes hacer reservas para fechas pasadas');
        return;
    }

    // Crear el modal para la selección de hora
    const modal = document.createElement('div');
    modal.className = 'modal-overlay active';

    // Contenido del modal
    modal.innerHTML = `
  <div class="modal-container">
    <div class="modal-header">
      <div>
        <i class="fas fa-clock modal-icon"></i>
        <span class="modal-title">Seleccionar Hora</span>
      </div>
    </div>
    <div class="modal-content">
      <p>Selecciona la hora para tu reserva del día ${day}/${month + 1}/${year}</p>
      <p class="time-guide">El restaurante opera de 17:00 a 2:00. Reservas disponibles hasta las 23:00</p>
      <div class="time-slots">
        ${generateTimeSlots(dateSelected)}
      </div>
    </div>
    <div class="modal-actions">
      <button id="cancelModalBtn" class="modal-btn modal-btn-cancel">Cancelar</button>
    </div>
  </div>
`;

    // Añadir el modal al body
    document.body.appendChild(modal);

    // Configurar el botón de cancelar
    modal.querySelector('#cancelModalBtn').addEventListener('click', function() {
        document.body.removeChild(modal);
    });

    // Cerrar el modal al hacer clic fuera de él
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            document.body.removeChild(modal);
        }
    });

    // Configurar los time slots
    const timeSlots = modal.querySelectorAll('.time-slot');
    timeSlots.forEach(slot => {
        slot.addEventListener('click', function() {
            const timeValue = this.getAttribute('data-time');
            selectReservationTime(dateSelected, timeValue);
            document.body.removeChild(modal);
        });
    });
}

/**
 * Genera slots de tiempo disponibles para la fecha seleccionada
 */
function generateTimeSlots(date) {
    let html = '';
    // Generar slots cada 30 minutos desde las 17:00 hasta las 22:30
    for (let hour = 17; hour < 23; hour++) {
        for (let minute of [0, 30]) {
            const timeDate = new Date(date);
            timeDate.setHours(hour, minute);

            // Solo mostrar horas futuras si es hoy
            const now = new Date();
            if (date.getDate() === now.getDate() &&
                date.getMonth() === now.getMonth() &&
                date.getFullYear() === now.getFullYear() &&
                timeDate <= now) {
                continue;
            }

            const timeString = formatTime(timeDate);
            const timeValue = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;

            html += `<div class="time-slot" data-time="${timeValue}">${timeString}</div>`;
        }
    }

    if (html === '') {
        return '<p class="no-slots">No hay horarios disponibles para hoy</p>';
    }

    return html;
}

/**
 * Formatea la hora en formato de 24 horas
 */
function formatTime(date) {
    return date.toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' });
}

/**
 * Redirige al formulario de creación de reserva con la fecha y hora seleccionadas
 */
function selectReservationTime(date, timeValue) {
    const [hours, minutes] = timeValue.split(':').map(Number);
    date.setHours(hours, minutes);

    // Formatear la fecha para el parámetro de URL
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    const formattedTime = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`;

    // Redirigir a la página de creación de reserva con la fecha/hora preseleccionada
    window.location.href = `/reservations/create?date=${year}-${month}-${day}T${formattedTime}`;
}

/**
 * Verifica si una hora está dentro del horario de operación del restaurante
 */
function isValidReservationTime(date) {
    const hours = date.getHours();
    // El restaurante opera de 17:00 (5pm) a 2:00am
    // Las reservas se pueden hacer hasta las 23:00 (11pm)
    return hours >= 17 && hours < 23;
}