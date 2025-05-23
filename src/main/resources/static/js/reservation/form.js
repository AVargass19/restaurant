function validateReservationTime(input) {
    const selectedDateTime = new Date(input.value);
    const hours = selectedDateTime.getHours();
    const errorDiv = document.getElementById('timeError');

    // Verificar si la hora está dentro del horario operativo (17:00 - 23:00)
    if (hours < 17 || hours >= 23) {
        errorDiv.style.display = 'block';
        input.setCustomValidity('La hora de reserva debe estar entre las 17:00 y las 23:00');
        return false;
    } else {
        errorDiv.style.display = 'none';
        input.setCustomValidity('');
        return true;
    }
}

function validateReservationTimeAndUpdateTables(input) {
    if (validateReservationTime(input)) {
        updateAvailableTables(input.value);
    }
}

function updateAvailableTables(dateTimeValue) {
    const tableSelect = document.getElementById('tableId');
    const loadingIndicator = document.getElementById('loadingTables');
    const noTablesMessage = document.getElementById('noTablesAvailable');

    // Guardar la selección actual si hay alguna
    const currentSelection = tableSelect.value;

    // Mostrar indicador de carga
    loadingIndicator.style.display = 'block';
    noTablesMessage.style.display = 'none';

    // Desactivar selector mientras se carga
    tableSelect.disabled = true;

    // Realizar la solicitud AJAX para obtener mesas disponibles
    fetch(`/reservations/available-tables?date=${encodeURIComponent(dateTimeValue)}`)
        .then(response => response.json())
        .then(tables => {
            // Limpiar opciones actuales excepto la primera (placeholder)
            while (tableSelect.options.length > 1) {
                tableSelect.remove(1);
            }

            // Ordenar las mesas por ID (orden ascendente)
            tables.sort((a, b) => a.id - b.id);

            // Añadir las mesas disponibles
            if (tables && tables.length > 0) {
                tables.forEach(table => {
                    const option = document.createElement('option');
                    option.value = table.id;
                    option.text = 'Mesa ' + table.id;
                    tableSelect.add(option);
                });

                // Intentar restaurar la selección anterior si existe en las nuevas opciones
                if (currentSelection) {
                    const exists = Array.from(tableSelect.options).some(option => option.value === currentSelection);
                    if (exists) {
                        tableSelect.value = currentSelection;
                    }
                }

                noTablesMessage.style.display = 'none';
            } else {
                // Mostrar mensaje si no hay mesas disponibles
                noTablesMessage.style.display = 'block';
            }
        })
        .catch(error => {
            console.error('Error al obtener mesas disponibles:', error);
            noTablesMessage.style.display = 'block';
        })
        .finally(() => {
            // Ocultar indicador de carga y habilitar selector
            loadingIndicator.style.display = 'none';
            tableSelect.disabled = false;
        });
}

// Validar y actualizar mesas al cargar la página si ya hay una fecha seleccionada
document.addEventListener('DOMContentLoaded', function() {
    const dateInput = document.getElementById('date');
    if (dateInput.value) {
        validateReservationTime(dateInput);
    }
});