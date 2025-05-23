/**
 * JavaScript para el historial de reservas
 */

// Variables globales
let allHistoryRows = [];

// Función principal que se ejecuta cuando el DOM está completamente cargado
document.addEventListener('DOMContentLoaded', function() {
    try {
        // Almacenar todas las filas para la búsqueda en tiempo real
        allHistoryRows = Array.from(document.querySelectorAll('#historyTableBody tr.history-row') || []);

        // Configurar eventos y funcionalidad
        setupSearchFunctionality();
        setupModalFunctionality();
        setupResponsiveTable();
        initializeClearButton();
    } catch (error) {
        console.error('Error durante la inicialización:', error);
    }
});

/**
 * Configura la funcionalidad de búsqueda en tiempo real
 */
function setupSearchFunctionality() {
    try {
        const searchInput = document.getElementById('searchQuery');
        const noRecordsMessage = document.getElementById('noRecordsMessage');

        if (!searchInput) return;

        // Evento para búsqueda en tiempo real mientras se escribe
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();
            let visibleRowsCount = 0;

            // Mostrar/ocultar botón de limpiar
            const clearButton = document.getElementById('clearSearch');
            if (clearButton) {
                clearButton.style.display = searchTerm ? 'block' : 'none';
            }

            // Si no hay filas o no están inicializadas, salir
            if (!allHistoryRows || allHistoryRows.length === 0) return;

            // Filtrar filas según el término de búsqueda
            allHistoryRows.forEach(row => {
                const textContent = row.textContent.toLowerCase();
                const shouldShow = searchTerm === '' || textContent.includes(searchTerm);

                row.style.display = shouldShow ? '' : 'none';
                if (shouldShow) visibleRowsCount++;
            });

            // Mostrar mensaje si no hay resultados
            if (noRecordsMessage) {
                noRecordsMessage.style.display = visibleRowsCount === 0 ? 'block' : 'none';
            }
        });

        // Evento para envío del formulario al presionar Enter
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const searchForm = document.getElementById('searchForm');
                if (searchForm) searchForm.submit();
            }
        });
    } catch (error) {
        console.error('Error al configurar la búsqueda:', error);
    }
}

/**
 * Configura el botón de limpiar búsqueda
 */
function initializeClearButton() {
    try {
        const clearButton = document.getElementById('clearSearch');
        const searchInput = document.getElementById('searchQuery');

        if (!clearButton || !searchInput) return;

        // Mostrar/ocultar botón según si hay texto
        clearButton.style.display = searchInput.value ? 'block' : 'none';

        // Configurar evento de clic para limpiar
        clearButton.addEventListener('click', function() {
            searchInput.value = '';
            searchInput.focus();

            // Disparar evento input para actualizar resultados
            const inputEvent = new Event('input', { bubbles: true });
            searchInput.dispatchEvent(inputEvent);

            // Ocultar botón
            this.style.display = 'none';
        });
    } catch (error) {
        console.error('Error al inicializar el botón de limpiar:', error);
    }
}

/**
 * Configura la responsividad de la tabla para dispositivos móviles
 */
function setupResponsiveTable() {
    const table = document.querySelector('.history-table');
    if (!table) return;

    // Añadir class para mejor visualización móvil
    table.classList.add('responsive-table');

    // Verificar ancho de pantalla para ajustes
    adjustTableForScreenSize();

    // Ajustar cuando cambia el tamaño de la ventana
    window.addEventListener('resize', adjustTableForScreenSize);
}

/**
 * Ajusta la tabla según el tamaño de la pantalla
 */
function adjustTableForScreenSize() {
    const isMobile = window.innerWidth < 768;
    const table = document.querySelector('.history-table');

    if (table) {
        table.classList.toggle('mobile-view', isMobile);
    }
}

/**
 * Función para cambiar el tamaño de la página
 * @param {string} size - Tamaño de página opcional (para el selector inferior)
 */
function changePageSize(size) {
    try {
        // Si se proporciona un valor, usarlo; si no, obtenerlo del selector principal
        const pageSize = size || document.getElementById('pageSize')?.value || '15';

        // Sincronizar ambos selectores si existe el segundo
        const bottomSelector = document.getElementById('pageSizeBottom');
        const topSelector = document.getElementById('pageSize');

        if (bottomSelector && !size && topSelector) {
            bottomSelector.value = topSelector.value;
        } else if (topSelector && size) {
            topSelector.value = pageSize;
        }

        // Actualizar URL con el nuevo tamaño de página
        const currentUrl = new URL(window.location.href);
        currentUrl.searchParams.set('size', pageSize);
        currentUrl.searchParams.set('page', '1'); // Volver a primera página

        // Redirigir
        window.location.href = currentUrl.toString();
    } catch (error) {
        console.error('Error al cambiar el tamaño de la página:', error);
        // Método alternativo si hay error
        window.location.href = `/history?page=1&size=${size || '15'}`;
    }
}


/**
 * Formatea una marca de tiempo para visualización
 * @param {string} timestamp - Marca de tiempo ISO
 * @returns {string} - Marca de tiempo formateada
 */
function formatTimestamp(timestamp) {
    if (!timestamp) return 'Fecha desconocida';

    try {
        // Para fechas ISO estándar
        if (typeof timestamp === 'string' && timestamp.includes('T')) {
            const date = new Date(timestamp);

            if (isNaN(date.getTime())) {
                return timestamp; // Si no se puede parsear
            }

            // Formatear como DD/MM/YYYY HH:MM
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');

            return `${day}/${month}/${year} ${hours}:${minutes}`;
        }

        // Si ya está en formato dd/MM/yyyy HH:mm
        if (typeof timestamp === 'string' && timestamp.match(/^\d{2}\/\d{2}\/\d{4}/)) {
            return timestamp;
        }

        return timestamp;
    } catch (e) {
        console.error('Error al formatear timestamp:', e);
        return timestamp || 'Fecha desconocida';
    }
}