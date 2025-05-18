/**
 * Funciones JavaScript para la gestión de mesas en el dashboard
 */

// Variables globales
let currentViewDate = new Date();

// Inicialización de la vista de mesas
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar la vista de mesas por día para el staff si existe
    if (document.querySelector('.table-grid')) {
        setupTableNavigation();
    }
});

/**
 * Configura la navegación de la vista de mesas
 */
function setupTableNavigation() {
    // Agregar controles de navegación para las mesas si no existen
    const tablePanel = document.querySelector('.table-grid').parentElement;

    if (tablePanel && !document.getElementById('tableNavControls')) {
        // Insertar controles de navegación antes de la cuadrícula de mesas
        const navControls = document.createElement('div');
        navControls.id = 'tableNavControls';
        navControls.className = 'table-nav-controls';
        navControls.innerHTML = `
      <div class="date-nav">
        <button id="prevDayBtn" class="nav-btn"><i class="fas fa-chevron-left"></i> Día anterior</button>
        <h3 id="currentDateDisplay"></h3>
        <button id="nextDayBtn" class="nav-btn">Día siguiente <i class="fas fa-chevron-right"></i></button>
      </div>
      <div class="week-nav">
        <button id="viewWeekBtn" class="week-btn">Ver semana completa</button>
      </div>
    `;

        // Insertar antes de la cuadrícula de mesas
        tablePanel.insertBefore(navControls, document.querySelector('.table-grid'));

        // Configurar eventos para los botones de navegación
        document.getElementById('prevDayBtn').addEventListener('click', function() {
            navigateTableDay(-1);
        });

        document.getElementById('nextDayBtn').addEventListener('click', function() {
            navigateTableDay(1);
        });

        document.getElementById('viewWeekBtn').addEventListener('click', function() {
            showWeekView();
        });

        // Inicializar la vista
        updateTablesView();
    }
}

/**
 * Actualiza la visualización de mesas para la fecha actual
 */

/**
 * Actualiza la visualización de mesas para la fecha actual
 */
function updateTablesView() {
    const tableGrid = document.querySelector('.table-grid');
    if (!tableGrid) return;

    const dateDisplay = document.getElementById('currentDateDisplay');
    if (dateDisplay) {
        // Formatear la fecha actual en español
        const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        dateDisplay.textContent = currentViewDate.toLocaleDateString('es-ES', options);
    }

    // Realizar una petición AJAX para obtener el estado real de las mesas
    const formattedDate = currentViewDate.toISOString().split('T')[0]; // Formato YYYY-MM-DD

    fetch(`/api/tables/status?date=${formattedDate}`)
        .then(response => response.json())
        .then(data => {
            // Actualizar cada mesa con su estado real
            const tables = tableGrid.querySelectorAll('.table-card');

            tables.forEach(tableCard => {
                // Extraer el ID de la mesa del texto (asumiendo que tiene formato "Mesa X")
                const tableText = tableCard.querySelector('div:nth-child(1)').textContent;
                const tableId = tableText.replace(/\D/g, ''); // Extraer solo los dígitos

                if (tableId && data[tableId]) {
                    const status = data[tableId].toLowerCase();

                    // Actualizar clase y texto
                    tableCard.className = `table-card ${status}`;
                    const statusText = tableCard.querySelector('div:nth-child(2)');
                    if (statusText) {
                        statusText.textContent = 'Status: ' + status.charAt(0).toUpperCase() + status.slice(1);
                    }
                }
            });
        })
        .catch(error => {
            console.error('Error al obtener el estado de las mesas:', error);
        });
}

/**
 * Navega a un día anterior o siguiente en la vista de mesas
 */
function navigateTableDay(direction) {
    // Crear una nueva fecha para no modificar directamente la fecha actual
    const newDate = new Date(currentViewDate);
    newDate.setDate(newDate.getDate() + direction);
    currentViewDate = newDate;

    updateTablesView();
}

/**
 * Muestra la vista de semana completa para las mesas
 */
function showWeekView() {
    // Obtener el contenedor de la cuadrícula de mesas
    const tablePanel = document.querySelector('.panel');
    if (!tablePanel) return;

    // Crear un modal para mostrar la vista semanal
    const modal = document.createElement('div');
    modal.className = 'modal-overlay active';

    // Obtener la fecha del primer día de la semana actual (lunes)
    const startDate = new Date(currentViewDate);
    const day = startDate.getDay(); // 0 = domingo, 1 = lunes, ..., 6 = sábado
    const diff = startDate.getDate() - day + (day === 0 ? -6 : 1); // Ajustar al lunes
    startDate.setDate(diff);

    // Generar la vista para cada día de la semana
    let weekHTML = '';
    const weekdays = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

    // Contenido del modal (estructura)
    modal.innerHTML = `
      <div class="modal-container week-view-modal">
        <div class="modal-header">
          <div>
            <i class="fas fa-calendar-week modal-icon"></i>
            <span class="modal-title">Vista semanal de mesas</span>
          </div>
        </div>
        <div class="modal-content">
          <div class="week-grid" id="weekGrid">
            <div class="loading-indicator">
              <i class="fas fa-spinner fa-spin"></i> Cargando datos de la semana...
            </div>
          </div>
        </div>
        <div class="modal-actions">
          <button id="closeWeekViewBtn" class="modal-btn modal-btn-cancel">Cerrar</button>
        </div>
      </div>
    `;

    // Añadir el modal al body
    document.body.appendChild(modal);

    // Configurar el botón de cierre
    document.getElementById('closeWeekViewBtn').addEventListener('click', function() {
        document.body.removeChild(modal);
    });

    // Cerrar el modal al hacer clic fuera de él
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            document.body.removeChild(modal);
        }
    });

    // Obtener datos para cada día de la semana
    const weekGrid = document.getElementById('weekGrid');

    // Crear el HTML para cada día de la semana
    let weekContentHTML = '';
    const promises = [];

    // Por cada día de la semana, obtener los datos de las mesas
    for (let i = 0; i < 7; i++) {
        const currentDate = new Date(startDate);
        currentDate.setDate(startDate.getDate() + i);

        // Formatear la fecha para mostrar y para la API
        const formattedDateDisplay = currentDate.toLocaleDateString('es-ES', {day: '2-digit', month: '2-digit'});
        const formattedDateForApi = currentDate.toISOString().split('T')[0]; // YYYY-MM-DD

        // Crear contenedor del día
        weekContentHTML += `
          <div class="week-day">
            <h4>${weekdays[i]} (${formattedDateDisplay})</h4>
            <div class="week-tables" id="day-${i}">
              <div class="loading-day"><i class="fas fa-spinner fa-spin"></i> Cargando...</div>
            </div>
          </div>
        `;

        // Preparar la promesa para obtener datos de este día
        const promise = fetch(`/api/tables/status?date=${formattedDateForApi}`)
            .then(response => response.json())
            .then(data => {
                return { dayIndex: i, tableData: data };
            });

        promises.push(promise);
    }

    // Reemplazar el contenido del grid con los días
    weekGrid.innerHTML = weekContentHTML;

    // Resolver todas las promesas y actualizar cada día
    Promise.all(promises)
        .then(results => {
            // Para cada día, actualizar su contenido con los datos reales
            results.forEach(result => {
                const dayContainer = document.getElementById(`day-${result.dayIndex}`);

                // Obtener todas las mesas y ordenarlas por ID
                const tableIds = Object.keys(result.tableData).sort((a, b) => parseInt(a) - parseInt(b));

                if (tableIds.length > 0) {
                    let tablesHTML = '';

                    // Crear una tarjeta por cada mesa
                    tableIds.forEach(tableId => {
                        const status = result.tableData[tableId].toLowerCase();
                        tablesHTML += `
                          <div class="table-card ${status}">Mesa ${tableId}</div>
                        `;
                    });

                    dayContainer.innerHTML = tablesHTML;
                } else {
                    dayContainer.innerHTML = '<div class="no-tables">No hay datos disponibles</div>';
                }
            });
        })
        .catch(error => {
            console.error('Error al obtener datos de la semana:', error);
            weekGrid.innerHTML = '<div class="error-message">Error al cargar los datos de la semana</div>';
        });
}