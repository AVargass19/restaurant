// table-scripts.js
document.addEventListener('DOMContentLoaded', function() {
    // Inicializa los filtros si están presentes
    if (document.getElementById('tableSearch')) {
        filterTables();
    }

    // Configuración de tooltips para los botones de acción
    const actionButtons = document.querySelectorAll('.action-btn');
    actionButtons.forEach(button => {
        const title = button.getAttribute('title');
        if (title) {
            button.addEventListener('mouseenter', function(e) {
                showTooltip(e, title);
            });

            button.addEventListener('mouseleave', function() {
                hideTooltip();
            });
        }
    });
});

// Función para filtrar las tablas
function filterTables() {
    const searchInput = document.getElementById('tableSearch').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    const rows = document.querySelectorAll('.table-row');
    let visibleCount = 0;

    rows.forEach(row => {
        const tableId = row.cells[0].textContent.toLowerCase();
        const status = row.getAttribute('data-status');

        const matchesSearch = tableId.includes(searchInput);
        const matchesStatus = statusFilter === 'ALL' || status === statusFilter;

        if (matchesSearch && matchesStatus) {
            row.style.display = '';
            visibleCount++;
        } else {
            row.style.display = 'none';
        }
    });

    // Mostrar/ocultar mensaje de "no hay datos"
    document.getElementById('noTablesMessage').style.display = visibleCount === 0 ? 'flex' : 'none';
}

// Verificar disponibilidad de mesas al cambiar la fecha y hora
function checkTableAvailability(dateInput) {
    const selectedDate = dateInput.value;
    if (!selectedDate) return;

    const loadingIndicator = document.getElementById('loadingTables');
    const noTablesMessage = document.getElementById('noTablesAvailable');
    const tableSelect = document.getElementById('tableId');

    if (loadingIndicator) loadingIndicator.style.display = 'block';
    if (tableSelect) tableSelect.disabled = true;

    // Simulando una llamada AJAX a backend
    setTimeout(() => {
        // Esta es una simulación. En un entorno real, aquí haría una petición AJAX
        // al backend para obtener las mesas disponibles para la fecha seleccionada

        if (loadingIndicator) loadingIndicator.style.display = 'none';
        if (tableSelect) tableSelect.disabled = false;

        // Comprobar si hay mesas disponibles (simulado)
        const hasAvailableTables = Math.random() > 0.3; // 70% probabilidad de tener mesas

        if (noTablesMessage) {
            noTablesMessage.style.display = hasAvailableTables ? 'none' : 'block';
        }
    }, 1000);
}

// Función para mostrar tooltip
function showTooltip(e, text) {
    // Eliminar cualquier tooltip existente
    hideTooltip();

    // Crear el tooltip
    const tooltip = document.createElement('div');
    tooltip.className = 'tooltip';
    tooltip.innerText = text;
    document.body.appendChild(tooltip);

    // Posicionar el tooltip
    const rect = e.target.getBoundingClientRect();
    tooltip.style.top = (rect.top - tooltip.offsetHeight - 5) + 'px';
    tooltip.style.left = (rect.left + (rect.width/2) - (tooltip.offsetWidth/2)) + 'px';

    // Mostrar el tooltip con una animación suave
    setTimeout(() => {
        tooltip.classList.add('visible');
    }, 10);
}

// Función para ocultar tooltip
function hideTooltip() {
    const tooltips = document.querySelectorAll('.tooltip');
    tooltips.forEach(tooltip => {
        tooltip.classList.remove('visible');
        setTimeout(() => {
            if (tooltip.parentNode) {
                tooltip.parentNode.removeChild(tooltip);
            }
        }, 300);
    });
}