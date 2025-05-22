document.addEventListener('DOMContentLoaded', function() {
    // Búsqueda en tiempo real
    const searchInput = document.getElementById('reservationSearch');
    const statusFilter = document.getElementById('statusFilter');
    const rows = document.querySelectorAll('.reservation-row');

    // Función para filtrar las reservas
    function filterReservations() {
        const searchTerm = searchInput.value.toLowerCase();
        const statusValue = statusFilter.value;

        rows.forEach(row => {
            const rowData = row.textContent.toLowerCase();
            const rowStatus = row.getAttribute('data-status');

            // Comprobar si la fila coincide con el término de búsqueda
            const matchesSearch = rowData.includes(searchTerm);

            // Comprobar si la fila coincide con el filtro de estado
            const matchesStatus = statusValue === 'all' || rowStatus === statusValue;

            // Mostrar u ocultar la fila según los filtros
            if (matchesSearch && matchesStatus) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });

        // Mostrar mensaje cuando no hay resultados
        checkNoResults();
    }

    // Mostrar mensaje cuando no hay resultados visibles
    function checkNoResults() {
        let visibleRows = 0;
        rows.forEach(row => {
            if (row.style.display !== 'none') {
                visibleRows++;
            }
        });

        const noDataMessage = document.querySelector('.no-data-message');
        if (noDataMessage) {
            if (visibleRows === 0) {
                noDataMessage.style.display = 'flex';
                noDataMessage.querySelector('p').textContent = 'No se encontraron reservas con los filtros actuales';
            } else {
                noDataMessage.style.display = 'none';
            }
        }
    }

    // Eventos para los filtros
    searchInput.addEventListener('input', filterReservations);
    statusFilter.addEventListener('change', filterReservations);

    // Resaltar filas al pasar el mouse
    rows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.classList.add('row-highlight');
        });
        row.addEventListener('mouseleave', function() {
            this.classList.remove('row-highlight');
        });
    });

    // Inicializar
    checkNoResults();
});