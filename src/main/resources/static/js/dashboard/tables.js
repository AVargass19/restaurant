/**
 * Inicializa la vista ordenando las mesas por ID desde el principio
 */
function initializeOrderedTablesView() {
    const tableGrid = document.querySelector('.table-grid');
    if (!tableGrid) return;

    const tables = Array.from(tableGrid.querySelectorAll('.table-card'));

    // Ordenar las mesas por ID
    tables.sort((a, b) => {
        const idA = parseInt(a.querySelector('div:nth-child(1)').textContent.replace(/\D/g, '')) || 0;
        const idB = parseInt(b.querySelector('div:nth-child(1)').textContent.replace(/\D/g, '')) || 0;
        return idA - idB;
    });

    // Limpiar el grid y volver a insertar en orden
    tableGrid.innerHTML = '';
    tables.forEach(table => {
        tableGrid.appendChild(table);
    });
}

/**
 * Funciones JavaScript para la gestión de mesas en el dashboard
 * Versión estática - Solo muestra información del día actual
 */

// Inicialización de la vista de mesas
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar la vista de mesas para el staff si existe
    if (document.querySelector('.table-grid')) {
        setupStaticTableView();
    }
});

/**
 * Configura la vista estática de mesas para el día actual
 */
function setupStaticTableView() {
    const tablePanel = document.querySelector('.table-grid').parentElement;

    if (tablePanel && !document.getElementById('currentDateDisplay')) {
        // Insertar solo el display de fecha actual antes de la cuadrícula de mesas
        const dateDisplay = document.createElement('div');
        dateDisplay.id = 'currentDateDisplay';
        dateDisplay.className = 'current-date-display';

        // Formatear la fecha actual en español
        const today = new Date();
        const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        const formattedDate = today.toLocaleDateString('es-ES', options);

        dateDisplay.innerHTML = `
            <div class="date-header" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding: 10px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #007bff;">
                <h3 style="margin: 0; color: #495057; font-size: 1.1rem;"><i class="fas fa-calendar-day" style="margin-right: 8px; color: #007bff;"></i> ${formattedDate}</h3>
                <button id="refreshTablesBtn" class="refresh-btn" title="Actualizar estado de mesas" style="background: #007bff; color: white; border: none; padding: 6px 12px; border-radius: 5px; cursor: pointer; font-size: 0.85rem; transition: all 0.2s;">
                    <i class="fas fa-sync-alt" style="margin-right: 5px;"></i> Actualizar
                </button>
            </div>
        `;

        // Insertar antes de la cuadrícula de mesas
        tablePanel.insertBefore(dateDisplay, document.querySelector('.table-grid'));

        // Configurar evento para el botón de actualización
        document.getElementById('refreshTablesBtn').addEventListener('click', function() {
            updateTablesStatus();
        });

        // Añadir estilos hover para el botón
        const refreshBtn = document.getElementById('refreshTablesBtn');
        refreshBtn.addEventListener('mouseenter', function() {
            this.style.background = '#0056b3';
            this.style.transform = 'translateY(-1px)';
        });
        refreshBtn.addEventListener('mouseleave', function() {
            this.style.background = '#007bff';
            this.style.transform = 'translateY(0)';
        });

        // Inicializar la vista con datos actuales y ordenamiento
        initializeOrderedTablesView();
        updateTablesStatus();

        // Configurar actualización automática cada 5 minutos (solo si hay endpoint disponible)
        setInterval(() => {
            // Solo actualizar automáticamente si no hay errores recientes
            const errorMessage = document.getElementById('errorMessage');
            if (!errorMessage) {
                updateTablesStatus();
            }
        }, 300000); // 5 minutos
    }
}

/**
 * Actualiza el estado de las mesas con información real del día actual
 */
function updateTablesStatus() {
    const tableGrid = document.querySelector('.table-grid');
    if (!tableGrid) return;

    // Añadir indicador de carga
    const refreshBtn = document.getElementById('refreshTablesBtn');
    if (refreshBtn) {
        const icon = refreshBtn.querySelector('i');
        icon.classList.add('fa-spin');
        refreshBtn.disabled = true;
    }

    // Intentar obtener datos del servidor, pero si falla, usar los datos del HTML
    const today = new Date();
    const formattedDate = today.toISOString().split('T')[0];

    // Verificar si el endpoint existe antes de hacer la llamada
    fetch(`/api/tables/status?date=${formattedDate}`)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            updateTablesWithData(data);
            updateLastRefreshTime();
        })
        .catch(error => {
            console.log('API no disponible, usando datos del HTML:', error.message);
            // En lugar de mostrar error, simplemente usar los datos que ya están en el HTML
            updateTablesFromHTML();
            updateLastRefreshTime();
        })
        .finally(() => {
            // Remover indicador de carga
            if (refreshBtn) {
                const icon = refreshBtn.querySelector('i');
                icon.classList.remove('fa-spin');
                refreshBtn.disabled = false;
            }
        });
}

/**
 * Actualiza las mesas con datos del servidor
 */
function updateTablesWithData(data) {
    const tableGrid = document.querySelector('.table-grid');
    const tables = Array.from(tableGrid.querySelectorAll('.table-card'));

    // Crear un array con las mesas y sus datos para ordenar
    const tablesWithData = tables.map(tableCard => {
        const tableText = tableCard.querySelector('div:nth-child(1)').textContent;
        const tableId = tableText.replace(/\D/g, '');
        const numericId = parseInt(tableId) || 0;

        let status = 'available';
        let additionalInfo = null;

        if (tableId && data[tableId]) {
            status = data[tableId].toLowerCase();
            additionalInfo = data[tableId + '_info'];
        }

        return {
            element: tableCard,
            id: numericId,
            status: status,
            additionalInfo: additionalInfo
        };
    });

    // Ordenar por ID ascendente
    tablesWithData.sort((a, b) => a.id - b.id);

    // Limpiar el grid y volver a insertar las mesas en orden
    tableGrid.innerHTML = '';

    tablesWithData.forEach(tableData => {
        updateTableCard(tableData.element, tableData.status, tableData.additionalInfo);
        tableGrid.appendChild(tableData.element);
    });
}

/**
 * Actualiza las mesas usando los datos que ya están en el HTML
 */
function updateTablesFromHTML() {
    const tableGrid = document.querySelector('.table-grid');
    const tables = Array.from(tableGrid.querySelectorAll('.table-card'));

    // Ordenar las mesas por ID antes de procesarlas
    tables.sort((a, b) => {
        const idA = parseInt(a.querySelector('div:nth-child(1)').textContent.replace(/\D/g, '')) || 0;
        const idB = parseInt(b.querySelector('div:nth-child(1)').textContent.replace(/\D/g, '')) || 0;
        return idA - idB;
    });

    // Limpiar el grid y volver a insertar las mesas en orden
    tableGrid.innerHTML = '';
    tables.forEach(tableCard => {
        // Determinar el estado actual basado en las clases CSS existentes
        let currentStatus = 'available';
        if (tableCard.classList.contains('occupied')) {
            currentStatus = 'occupied';
        } else if (tableCard.classList.contains('reserved')) {
            currentStatus = 'reserved';
        }

        // Mantener el estado actual y solo actualizar el formato del texto si es necesario
        updateTableCard(tableCard, currentStatus);
        tableGrid.appendChild(tableCard);
    });
}

/**
 * Actualiza una tarjeta de mesa individual
 */
function updateTableCard(tableCard, status, additionalInfo = null) {
    // Mapear estados a clases CSS y texto en español
    let cssClass = 'table-card';
    let statusDisplay = '';

    switch(status.toLowerCase()) {
        case 'available':
            cssClass += ' available';
            statusDisplay = 'Disponible';
            break;
        case 'occupied':
            cssClass += ' occupied';
            statusDisplay = 'Ocupada';
            break;
        case 'reserved':
            cssClass += ' reserved';
            statusDisplay = 'Reservada';
            break;
        default:
            cssClass += ' available';
            statusDisplay = 'Disponible';
    }

    // Actualizar clase y texto
    tableCard.className = cssClass;
    const statusText = tableCard.querySelector('div:nth-child(2)');
    if (statusText) {
        statusText.textContent = `Estado: ${statusDisplay}`;
    }

    // Añadir información adicional si existe
    if (additionalInfo) {
        let tooltipText = '';

        if (additionalInfo.reservationTime) {
            tooltipText += `Reserva: ${additionalInfo.reservationTime}`;
        }
        if (additionalInfo.customerName) {
            tooltipText += `${tooltipText ? '\n' : ''}Cliente: ${additionalInfo.customerName}`;
        }
        if (additionalInfo.guests) {
            tooltipText += `${tooltipText ? '\n' : ''}Comensales: ${additionalInfo.guests}`;
        }

        if (tooltipText) {
            tableCard.setAttribute('title', tooltipText);
        }
    }
}

/**
 * Actualiza el timestamp de la última actualización
 */
function updateLastRefreshTime() {
    let lastUpdateElement = document.getElementById('lastUpdateTime');

    if (!lastUpdateElement) {
        // Crear elemento si no existe
        const dateDisplay = document.getElementById('currentDateDisplay');
        if (dateDisplay) {
            lastUpdateElement = document.createElement('div');
            lastUpdateElement.id = 'lastUpdateTime';
            dateDisplay.appendChild(lastUpdateElement);
        }
    }

    if (lastUpdateElement) {
        const now = new Date();
        const timeString = now.toLocaleTimeString('es-ES', {
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
        lastUpdateElement.innerHTML = `<small style="color: #6c757d; font-size: 0.8rem; margin-top: 5px; display: block;">Última actualización: ${timeString}</small>`;
    }
}

/**
 * Muestra un mensaje de error temporal (eliminado - ya no se usa)
 */
function showErrorMessage(message) {
    // Función deshabilitada - ya no mostramos errores molestos
    console.log('Info:', message);
}

/**
 * Función auxiliar para formatear fecha
 */
function formatDate(date) {
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };
    return date.toLocaleDateString('es-ES', options);
}

/**
 * Función para limpiar y reinicializar la vista si es necesario
 */
function reinitializeTablesView() {
    // Limpiar elementos existentes
    const existingDisplay = document.getElementById('currentDateDisplay');
    if (existingDisplay) {
        existingDisplay.remove();
    }

    const existingError = document.getElementById('errorMessage');
    if (existingError) {
        existingError.remove();
    }

    // Reinicializar
    setupStaticTableView();
}