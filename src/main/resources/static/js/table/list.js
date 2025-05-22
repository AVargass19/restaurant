let tableIdToDelete = null;

function showDeleteModal(element) {
    tableIdToDelete = element.getAttribute('data-table-id');
    document.getElementById('tableIdToDelete').textContent = tableIdToDelete;
    document.getElementById('deleteModal').style.display = 'block';
}

function closeDeleteModal() {
    document.getElementById('deleteModal').style.display = 'none';
    tableIdToDelete = null;
}

function confirmDelete() {
    if (tableIdToDelete) {
        // Redirigir a la URL de eliminación
        window.location.href = '/tables/delete/' + tableIdToDelete;
    }
}

// Cerrar modal al hacer clic en la X
document.querySelector('.close').onclick = function() {
    closeDeleteModal();
}

// Cerrar modal al hacer clic fuera de él
window.onclick = function(event) {
    const modal = document.getElementById('deleteModal');
    if (event.target == modal) {
        closeDeleteModal();
    }
}

// Función de filtrado de tablas (mantener la funcionalidad existente)
function filterTables() {
    const searchInput = document.getElementById('tableSearch').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    const tableRows = document.querySelectorAll('.table-row');
    const noTablesMessage = document.getElementById('noTablesMessage');
    let visibleRows = 0;

    tableRows.forEach(row => {
        const tableId = row.cells[0].textContent.toLowerCase();
        const tableStatus = row.getAttribute('data-status');

        const matchesSearch = tableId.includes(searchInput);
        const matchesStatus = statusFilter === 'ALL' || tableStatus === statusFilter;

        if (matchesSearch && matchesStatus) {
            row.style.display = '';
            visibleRows++;
        } else {
            row.style.display = 'none';
        }
    });

    // Mostrar mensaje si no hay resultados
    if (visibleRows === 0) {
        noTablesMessage.style.display = 'block';
    } else {
        noTablesMessage.style.display = 'none';
    }
}