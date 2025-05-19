/**
 * Funciones JavaScript para los modales del dashboard
 */

// Inicialización de los modales cuando el DOM está listo
document.addEventListener('DOMContentLoaded', function() {
    setupCancelationModal();
    setupTableResponsiveness();
    setupChangeDetailsModal();

    // Configuración para ordenar las acciones recientes en orden descendente
    sortRecentActionsDescending();
});

/**
 * Configura el modal de detalles de cambios, asegurando que se muestre correctamente
 */
function setupChangeDetailsModal() {
    const modal = document.getElementById('changeDetailsModal');
    if (!modal) return;

    // Asegurar que el modal esté centrado en la página
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';

    // Aplicar estilos adicionales para garantizar que se muestre como modal
    document.querySelectorAll('.modal-overlay').forEach(modalOverlay => {
        modalOverlay.style.position = 'fixed';
        modalOverlay.style.top = '0';
        modalOverlay.style.left = '0';
        modalOverlay.style.width = '100%';
        modalOverlay.style.height = '100%';
        modalOverlay.style.zIndex = '1000';
        modalOverlay.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
        modalOverlay.style.display = 'none';
    });

    // Cerrar al hacer clic fuera del modal
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            closeChangeDetailsModal();
        }
    });

    // Cerrar con tecla Escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && modal.classList.contains('active')) {
            closeChangeDetailsModal();
        }
    });
}

/**
 * Configura el modal de cancelación de reservas
 */
function setupCancelationModal() {
    const modal = document.getElementById('cancelReservationModal');
    if (!modal) return;

    // Aplicar estilos similares a los del modal de detalles
    modal.style.display = 'flex';
    modal.style.justifyContent = 'center';
    modal.style.alignItems = 'center';

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
            modal.style.display = 'flex';
        });
    });

    // Cerrar el modal al hacer clic en "Cancelar"
    if (cancelBtn) {
        cancelBtn.addEventListener('click', function() {
            modal.classList.remove('active');
            modal.style.display = 'none';
        });
    }

    // Cerrar el modal al hacer clic fuera de él
    modal.addEventListener('click', function(e) {
        if (e.target === modal) {
            modal.classList.remove('active');
            modal.style.display = 'none';
        }
    });
}

/**
 * Ordena las acciones recientes por su ID en orden descendente
 */
function sortRecentActionsDescending() {
    const actionsTable = document.querySelector('.panel:nth-child(4) table tbody');
    if (actionsTable) {
        const rows = Array.from(actionsTable.querySelectorAll('tr'));

        // Ordenar las filas por el ID de historial (asumiendo que está almacenado como data-history-id)
        rows.sort((a, b) => {
            const btnA = a.querySelector('.btn-details');
            const btnB = b.querySelector('.btn-details');

            if (btnA && btnB) {
                // Si tenemos el ID como atributo data, lo usamos
                const idA = parseInt(btnA.getAttribute('data-history-id') || '0');
                const idB = parseInt(btnB.getAttribute('data-history-id') || '0');
                return idB - idA; // Orden descendente
            }

            // Si no tenemos el ID, ordenamos por fecha (primera columna)
            const dateA = new Date(a.cells[0].textContent.trim().replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$3-$2-$1'));
            const dateB = new Date(b.cells[0].textContent.trim().replace(/(\d{2})\/(\d{2})\/(\d{4})/, '$3-$2-$1'));
            return dateB - dateA;
        });

        // Reordenar las filas en la tabla
        rows.forEach(row => actionsTable.appendChild(row));
    }
}

/**
 * Función mejorada para mostrar el modal de detalles de cambios
 */
function showChangeDetailsModal(element) {
    // Obtener el modal y sus componentes
    const modal = document.getElementById('changeDetailsModal');
    if (!modal) return;

    const modalContent = modal.querySelector('.modal-content');

    // Obtener los datos del elemento
    const action = element.getAttribute('data-action');
    const oldValues = element.getAttribute('data-old-values');
    const newValues = element.getAttribute('data-new-values');
    const historyId = element.getAttribute('data-history-id') || '';
    const timestamp = element.getAttribute('data-timestamp') || '';
    const username = element.getAttribute('data-username') || '';

    // Reconstruir el contenido del modal
    modalContent.innerHTML = '';

    // Agregar información de meta datos
    const metaSection = document.createElement('div');
    metaSection.className = 'history-meta';
    metaSection.innerHTML = `
        <div class="history-meta-info">
            <div class="history-meta-item">
                <i class="fas fa-hashtag history-meta-icon"></i>
                <span>ID: ${historyId}</span>
            </div>
            <div class="history-meta-item">
                <i class="fas fa-user history-meta-icon"></i>
                <span>${username}</span>
            </div>
            <div class="history-meta-item">
                <i class="fas fa-clock history-meta-icon"></i>
                <span>${formatTimestamp(timestamp)}</span>
            </div>
        </div>
    `;
    modalContent.appendChild(metaSection);

    // Crear el contenedor para el tipo de acción
    const actionContainer = document.createElement('div');
    actionContainer.id = 'actionTypeContainer';

    let actionText = 'Desconocido';
    if (action === 'CREATE') actionText = 'Creación';
    else if (action === 'UPDATE') actionText = 'Actualización';
    else if (action === 'DELETE') actionText = 'Eliminación';

    actionContainer.innerHTML = `
        <h4>Tipo de Acción:</h4>
        <p id="actionType" class="${action}">${actionText}</p>
    `;
    modalContent.appendChild(actionContainer);

    // Crear el contenedor para la comparación de valores
    const comparisonContainer = document.createElement('div');
    comparisonContainer.className = 'values-comparison';

    // Procesar según el tipo de acción
    try {
        if (action === 'CREATE') {
            // Solo mostramos valores nuevos
            comparisonContainer.innerHTML = `
                <div class="comparison-column new-values" style="flex: 1;">
                    <div class="comparison-header">
                        <i class="fas fa-plus-circle"></i>
                        Valores Creados
                    </div>
                    <div class="comparison-content" id="newValuesContent"></div>
                </div>
            `;
            modalContent.appendChild(comparisonContainer);

            const newValuesContent = document.getElementById('newValuesContent');
            const newValuesObj = newValues ? JSON.parse(newValues) : null;

            if (newValuesObj && Object.keys(newValuesObj).length > 0) {
                displayValuesWithHighlight(newValuesContent, newValuesObj, null, 'CREATE');
            } else {
                newValuesContent.innerHTML = '<div class="no-data-message">No hay datos disponibles</div>';
            }

        } else if (action === 'DELETE') {
            // Solo mostramos valores eliminados
            comparisonContainer.innerHTML = `
                <div class="comparison-column old-values" style="flex: 1;">
                    <div class="comparison-header">
                        <i class="fas fa-trash-alt"></i>
                        Valores Eliminados
                    </div>
                    <div class="comparison-content" id="oldValuesContent"></div>
                </div>
            `;
            modalContent.appendChild(comparisonContainer);

            const oldValuesContent = document.getElementById('oldValuesContent');
            const oldValuesObj = oldValues ? JSON.parse(oldValues) : null;

            if (oldValuesObj && Object.keys(oldValuesObj).length > 0) {
                displayValuesWithHighlight(oldValuesContent, oldValuesObj, null, 'DELETE');
            } else {
                oldValuesContent.innerHTML = '<div class="no-data-message">No hay datos disponibles</div>';
            }

        } else {
            // Para UPDATE, mostramos comparativa lado a lado
            comparisonContainer.innerHTML = `
                <div class="comparison-column old-values">
                    <div class="comparison-header">
                        <i class="fas fa-history"></i>
                        Valores Anteriores
                    </div>
                    <div class="comparison-content" id="oldValuesContent"></div>
                </div>
                <div class="comparison-column new-values">
                    <div class="comparison-header">
                        <i class="fas fa-edit"></i>
                        Valores Nuevos
                    </div>
                    <div class="comparison-content" id="newValuesContent"></div>
                </div>
            `;
            modalContent.appendChild(comparisonContainer);

            const oldValuesContent = document.getElementById('oldValuesContent');
            const newValuesContent = document.getElementById('newValuesContent');

            let oldValuesObj = null;
            let newValuesObj = null;

            if (oldValues && oldValues !== "null" && oldValues !== "undefined") {
                oldValuesObj = JSON.parse(oldValues);
            }

            if (newValues && newValues !== "null" && newValues !== "undefined") {
                newValuesObj = JSON.parse(newValues);
            }

            // Mostrar comparativa con resaltado
            displayComparisonTables(oldValuesContent, newValuesContent, oldValuesObj, newValuesObj);
        }
    } catch (error) {
        console.error('Error al procesar los datos para el modal:', error);
        modalContent.innerHTML += `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i>
                Error al procesar los datos: ${error.message}
            </div>
        `;
    }

    // Mostrar el modal
    modal.classList.add('active');
    modal.style.display = 'flex';
}

/**
 * Muestra tablas de comparación con campos resaltados que han cambiado
 */
function displayComparisonTables(oldContainer, newContainer, oldObj, newObj) {
    // Si no hay datos
    if (!oldObj && !newObj) {
        oldContainer.innerHTML = '<div class="no-data-message">No hay datos disponibles</div>';
        newContainer.innerHTML = '<div class="no-data-message">No hay datos disponibles</div>';
        return;
    }

    if (!oldObj) {
        oldContainer.innerHTML = '<div class="no-data-message">No hay datos anteriores</div>';
        displayValuesWithHighlight(newContainer, newObj, null, 'CREATE');
        return;
    }

    if (!newObj) {
        newContainer.innerHTML = '<div class="no-data-message">No hay datos nuevos</div>';
        displayValuesWithHighlight(oldContainer, oldObj, null, 'DELETE');
        return;
    }

    // Obtener todos los campos (de ambos objetos)
    const allFields = new Set([
        ...Object.keys(oldObj || {}),
        ...Object.keys(newObj || {})
    ]);

    // Crear tablas para ambos lados
    const oldTable = document.createElement('table');
    oldTable.className = 'comparison-table';
    oldTable.innerHTML = `
        <thead>
            <tr>
                <th>Campo</th>
                <th>Valor Anterior</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;

    const newTable = document.createElement('table');
    newTable.className = 'comparison-table';
    newTable.innerHTML = `
        <thead>
            <tr>
                <th>Campo</th>
                <th>Valor Nuevo</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;

    const oldTbody = oldTable.querySelector('tbody');
    const newTbody = newTable.querySelector('tbody');

    // Ordenar los campos alfabéticamente
    const sortedFields = Array.from(allFields).sort();

    // Agregar filas para cada campo
    sortedFields.forEach(field => {
        const oldValue = oldObj && oldObj[field] !== undefined ? oldObj[field] : 'N/A';
        const newValue = newObj && newObj[field] !== undefined ? newObj[field] : 'N/A';

        // Determinar si el valor cambió
        const hasChanged = JSON.stringify(oldValue) !== JSON.stringify(newValue);
        const wasAdded = oldObj[field] === undefined && newObj[field] !== undefined;
        const wasRemoved = oldObj[field] !== undefined && newObj[field] === undefined;

        // Crear fila para la tabla de valores antiguos
        const oldRow = document.createElement('tr');
        oldRow.innerHTML = `
            <td>${formatFieldName(field)}</td>
            <td class="${wasRemoved ? 'highlight-removed' : (hasChanged ? 'highlight-changed' : '')}">${formatFieldValue(oldValue)}</td>
        `;
        oldTbody.appendChild(oldRow);

        // Crear fila para la tabla de valores nuevos
        const newRow = document.createElement('tr');
        newRow.innerHTML = `
            <td>${formatFieldName(field)}</td>
            <td class="${wasAdded ? 'highlight-added' : (hasChanged ? 'highlight-changed' : '')}">${formatFieldValue(newValue)}</td>
        `;
        newTbody.appendChild(newRow);
    });

    // Agregar tablas a los contenedores
    oldContainer.appendChild(oldTable);
    newContainer.appendChild(newTable);
}

/**
 * Muestra valores en una tabla con posible resaltado
 */
function displayValuesWithHighlight(container, valuesObj, compareObj, action) {
    if (!valuesObj || Object.keys(valuesObj).length === 0) {
        container.innerHTML = '<div class="no-data-message">No hay datos disponibles</div>';
        return;
    }

    const table = document.createElement('table');
    table.className = 'comparison-table';

    // Crear encabezado
    let headerText = 'Valor';
    if (action === 'CREATE') headerText = 'Valor Creado';
    if (action === 'DELETE') headerText = 'Valor Eliminado';

    table.innerHTML = `
        <thead>
            <tr>
                <th>Campo</th>
                <th>${headerText}</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;

    const tbody = table.querySelector('tbody');

    // Ordenar las claves alfabéticamente
    const sortedKeys = Object.keys(valuesObj).sort();

    // Agregar filas
    sortedKeys.forEach(key => {
        if (valuesObj.hasOwnProperty(key)) {
            const row = document.createElement('tr');

            // Determinar si el valor cambió (si se proporciona objeto de comparación)
            let highlightClass = '';
            if (compareObj) {
                const hasChanged = JSON.stringify(valuesObj[key]) !== JSON.stringify(compareObj[key]);
                const exists = key in compareObj;

                if (!exists && action === 'CREATE') highlightClass = 'highlight-added';
                else if (!exists && action === 'DELETE') highlightClass = 'highlight-removed';
                else if (hasChanged) highlightClass = 'highlight-changed';
            } else if (action === 'CREATE') {
                highlightClass = 'highlight-added';
            } else if (action === 'DELETE') {
                highlightClass = 'highlight-removed';
            }

            row.innerHTML = `
                <td>${formatFieldName(key)}</td>
                <td class="${highlightClass}">${formatFieldValue(valuesObj[key])}</td>
            `;

            tbody.appendChild(row);
        }
    });

    container.appendChild(table);
}

/**
 * Formatea el nombre del campo para mejor legibilidad
 * MEJORADO: Más campos traducidos y mejor presentación
 */
function formatFieldName(key) {
    // Mapa mejorado de nombres de campos técnicos a nombres amigables
    const fieldNameMap = {
        'id': 'ID',
        'userId': 'ID de Usuario',
        'user_id': 'ID de Usuario',
        'tableId': 'ID de Mesa',
        'table_id': 'ID de Mesa',
        'tableNumber': 'Número de Mesa',
        'tableStatus': 'Estado de la Mesa',
        'guests': 'Comensales',
        'guests_count': 'Comensales',
        'status': 'Estado',
        'date': 'Fecha y Hora',
        'reservation_date': 'Fecha de la Reserva',
        'createdAt': 'Creado el',
        'created_at': 'Creado el',
        'updatedAt': 'Actualizado el',
        'updated_at': 'Actualizado el',
        'username': 'Usuario',
        'firstName': 'Nombre',
        'first_name': 'Nombre',
        'lastName': 'Apellido',
        'last_name': 'Apellido',
        'email': 'Correo Electrónico',
        'phone': 'Teléfono',
        'phoneNumber': 'Teléfono',
        'phone_number': 'Teléfono',
        'notes': 'Notas',
        'special_requests': 'Peticiones Especiales',
        'specialRequests': 'Peticiones Especiales',
        'reservationStatus': 'Estado de Reserva',
        'res_id': 'ID Reserva',
        'res_status': 'Estado de Reserva',
        'res_date': 'Fecha de Reserva',
        'res_guests': 'Comensales',
        'res_table': 'Mesa',
        'customer_name': 'Nombre del Cliente',
        'customer_id': 'ID del Cliente',
        'capacity': 'Capacidad',
        'is_active': 'Activo',
        'isActive': 'Activo',
        'role': 'Rol',
        'user_role': 'Rol de Usuario',
        'res_estado': 'Reserva Estado',
        'res_created_at': 'Fecha creación Reserva'
    };

    // Si el campo está en nuestro mapa, usar el nombre amigable
    if (fieldNameMap[key]) {
        return fieldNameMap[key];
    }

    // Para campos que no están en el mapa, mejorar la visualización
    // 1. Convertir snake_case a espacio separado
    let formattedName = key.replace(/_/g, ' ');

    // 2. Convertir camelCase a espacio separado
    formattedName = formattedName.replace(/([A-Z])/g, ' $1');

    // 3. Capitalizar primera letra de cada palabra
    formattedName = formattedName.replace(/\b\w/g, l => l.toUpperCase());

    return formattedName;
}

/**
 * Formatea el valor del campo para mejor legibilidad
 * MEJORADO: Mejor formato para fechas y estados
 */
function formatFieldValue(value) {
    if (value === null || value === undefined || value === 'N/A') {
        return '<span class="text-muted">No definido</span>';
    }

    // Formatear según el tipo de dato
    if (typeof value === 'boolean') {
        return value ? '<span class="text-success">Sí</span>' : '<span class="text-danger">No</span>';
    }

    if (typeof value === 'object') {
        // Si es una fecha JSON (string con formato de fecha)
        if (value.hasOwnProperty('date') || value.hasOwnProperty('timestamp')) {
            try {
                const dateStr = value.date || value.timestamp;
                const date = new Date(dateStr);
                return formatDate(date);
            } catch (e) {
                // Si falla, mostrar el objeto
            }
        }

        // Para objetos anidados
        try {
            const json = JSON.stringify(value, null, 2);
            return `<pre class="code-block">${json}</pre>`;
        } catch (e) {
            return 'Objeto complejo';
        }
    }

    // Formatear fechas si el valor parece una fecha ISO
    if (typeof value === 'string') {
        // Comprobar si es una fecha ISO
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(value)) {
            try {
                const date = new Date(value);
                return formatDate(date);
            } catch (e) {
                return value;
            }
        }

        // Si es un estado de reserva, traducir y aplicar estilo
        const statusMap = {
            'ACTIVE': 'ACTIVA',
            'PENDING': 'PENDIENTE',
            'COMPLETED': 'COMPLETADA',
            'CANCELLED': 'CANCELADA'
        };

        if (statusMap[value]) {
            const statusClasses = {
                'ACTIVE': 'success',
                'PENDING': 'warning',
                'COMPLETED': 'info',
                'CANCELLED': 'danger'
            };
            return `<span class="badge ${statusClasses[value] || ''}">${statusMap[value]}</span>`;
        }
    }

    return String(value);
}

/**
 * Formatea una fecha para mostrarla en formato legible
 */
function formatDate(date) {
    if (!(date instanceof Date) || isNaN(date)) {
        return 'Fecha inválida';
    }

    // Formatear como: DD/MM/YYYY HH:MM
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}/${month}/${year} ${hours}:${minutes}`;
}

/**
 * Formatea una timestamp para mostrarla de forma amigable
 */
function formatTimestamp(timestamp) {
    if (!timestamp) return 'Fecha desconocida';

    try {
        const date = new Date(timestamp);
        return formatDate(date);
    } catch (e) {
        return timestamp;
    }
}

/**
 * Cierra el modal de detalles de cambios
 */
function closeChangeDetailsModal() {
    const modal = document.getElementById('changeDetailsModal');
    if (modal) {
        modal.classList.remove('active');
        modal.style.display = 'none';
    }
}

/**
 * Configura la responsividad de las tablas para dispositivos móviles
 */
function setupTableResponsiveness() {
    // Marcar las columnas que deben ocultarse en dispositivos móviles
    const tables = document.querySelectorAll('table');

    tables.forEach(table => {
        // Buscar celdas de encabezado que contengan fechas o ID
        const headers = table.querySelectorAll('th');

        headers.forEach((header, index) => {
            const headerText = header.textContent.toLowerCase();

            // Detectar columnas por su contenido
            if (headerText.includes('id') || headerText.includes('código')) {
                markColumnForMobile(table, index, 'id-column');
            } else if (headerText.includes('fecha')) {
                markColumnForMobile(table, index, 'date-column');
            } else if (headerText.includes('accion')) {
                markColumnForMobile(table, index, 'actions-column');
            }
        });
    });
}

/**
 * Marca una columna completa para aplicar estilos responsivos
 */
function markColumnForMobile(table, columnIndex, className) {
    // Marcar el encabezado
    const headers = table.querySelectorAll('th');
    if (headers[columnIndex]) {
        headers[columnIndex].classList.add(className);
    }

    // Marcar todas las celdas de esa columna
    const rows = table.querySelectorAll('tbody tr');
    rows.forEach(row => {
        const cells = row.querySelectorAll('td');
        if (cells[columnIndex]) {
            cells[columnIndex].classList.add(className);
        }
    });
}

/**
 * Función para ordenar una tabla
 */
function sortTableByDate(tableId, columnIndex, ascending = false) {
    const table = document.getElementById(tableId);
    if (!table) return;

    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    rows.sort((a, b) => {
        const aValue = a.cells[columnIndex].textContent.trim();
        const bValue = b.cells[columnIndex].textContent.trim();

        // Convertir a fechas si son fechas
        if (aValue.match(/^\d{2}\/\d{2}\/\d{4}/)) {
            const [aDay, aMonth, aYear] = aValue.split('/').map(num => parseInt(num, 10));
            const [bDay, bMonth, bYear] = bValue.split('/').map(num => parseInt(num, 10));

            const aDate = new Date(aYear, aMonth - 1, aDay);
            const bDate = new Date(bYear, bMonth - 1, bDay);

            return ascending ? aDate - bDate : bDate - aDate;
        }

        // Ordenar como texto
        return ascending ? aValue.localeCompare(bValue) : bValue.localeCompare(aValue);
    });

    // Reordenar las filas en la tabla
    rows.forEach(row => tbody.appendChild(row));
}