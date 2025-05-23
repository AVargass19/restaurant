/**
 * JavaScript para la gestión de la lista de usuarios
 * Filtros y búsqueda en tiempo real
 */

document.addEventListener('DOMContentLoaded', function() {
    // Elementos del DOM
    const searchInput = document.getElementById('userSearch');
    const statusFilter = document.getElementById('statusFilter');
    const roleFilter = document.getElementById('roleFilter');
    const userRows = document.querySelectorAll('.user-row');

    // Elementos del modal
    const deleteModal = document.getElementById('deleteUserModal');
    const cancelDeleteBtn = document.getElementById('cancelDelete');
    const confirmDeleteBtn = document.getElementById('confirmDelete');
    const userToDeleteSpan = document.getElementById('userToDelete');

    let pendingDeleteUrl = null;

    // Función principal para filtrar usuarios
    function filterUsers() {
        const searchTerm = searchInput.value.toLowerCase();
        const statusValue = statusFilter.value;
        const roleValue = roleFilter.value;

        userRows.forEach(row => {
            // Obtener el texto de las celdas para búsqueda
            const rowData = row.textContent.toLowerCase();
            const rowStatus = row.getAttribute('data-status');
            const rowRole = row.getAttribute('data-role');

            // Verificar si coincide con la búsqueda
            const matchesSearch = rowData.includes(searchTerm);

            // Verificar si coincide con el filtro de estado
            const matchesStatus = statusValue === 'all' || rowStatus === statusValue;

            // Verificar si coincide con el filtro de rol
            const matchesRole = roleValue === 'all' || rowRole === roleValue;

            // Mostrar u ocultar la fila
            if (matchesSearch && matchesStatus && matchesRole) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });

        // Verificar si hay resultados visibles
        checkNoResults();
    }

    // Mostrar mensaje cuando no hay resultados
    function checkNoResults() {
        let visibleRows = 0;
        userRows.forEach(row => {
            if (row.style.display !== 'none') {
                visibleRows++;
            }
        });

        const noDataMessage = document.querySelector('.no-data-message');
        const tableContainer = document.querySelector('.table-responsive');

        if (noDataMessage && tableContainer) {
            if (visibleRows === 0) {
                tableContainer.style.display = 'none';
                noDataMessage.style.display = 'flex';
                noDataMessage.querySelector('p').textContent = 'No se encontraron usuarios con los filtros actuales';
            } else {
                tableContainer.style.display = 'block';
                noDataMessage.style.display = 'none';
            }
        }
    }

    // Configurar modal de eliminación
    function setupDeleteModal() {
        const deleteButtons = document.querySelectorAll('.action-btn.delete');

        deleteButtons.forEach(button => {
            // Remover cualquier onclick existente para evitar el alert
            button.removeAttribute('onclick');

            button.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();

                const row = this.closest('.user-row');
                const userName = row.querySelector('.name-column')?.textContent || '';
                const userLastName = row.querySelector('.lastname-column')?.textContent || '';
                const username = row.querySelector('.username-column')?.textContent || '';
                const fullName = `${userName} ${userLastName}`.trim() || username;

                // Guardar la URL de eliminación
                pendingDeleteUrl = this.href;

                // Mostrar información del usuario en el modal
                if (userToDeleteSpan) {
                    userToDeleteSpan.textContent = fullName;
                }

                // Mostrar modal
                showModal();
            });
        });
    }

    // Mostrar modal
    function showModal() {
        if (deleteModal) {
            deleteModal.style.display = 'flex';
            setTimeout(() => {
                deleteModal.classList.add('show');
            }, 10);
            document.body.style.overflow = 'hidden';
        }
    }

    // Cerrar modal
    function closeModal() {
        if (deleteModal) {
            deleteModal.classList.remove('show');
            setTimeout(() => {
                deleteModal.style.display = 'none';
                document.body.style.overflow = '';
                pendingDeleteUrl = null;
                resetConfirmButton();
            }, 300);
        }
    }

    // Confirmar eliminación
    function confirmDelete() {
        if (!pendingDeleteUrl) return;

        // Mostrar estado de carga
        if (confirmDeleteBtn) {
            confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Eliminando...';
            confirmDeleteBtn.disabled = true;
        }

        // Redirigir después de un breve delay
        setTimeout(() => {
            window.location.href = pendingDeleteUrl;
        }, 500);
    }

    // Resetear botón de confirmación
    function resetConfirmButton() {
        if (confirmDeleteBtn) {
            confirmDeleteBtn.innerHTML = '<i class="fas fa-trash-alt"></i> Eliminar Usuario';
            confirmDeleteBtn.disabled = false;
        }
    }

    // Auto-dismiss para alertas
    function setupAlertDismiss() {
        const alerts = document.querySelectorAll('.alert');
        alerts.forEach(alert => {
            setTimeout(() => {
                alert.style.opacity = '0';
                alert.style.transform = 'translateY(-10px)';
                setTimeout(() => alert.remove(), 300);
            }, 5000);
        });
    }

    // Event listeners
    if (searchInput) {
        searchInput.addEventListener('input', filterUsers);
    }

    if (statusFilter) {
        statusFilter.addEventListener('change', filterUsers);
    }

    if (roleFilter) {
        roleFilter.addEventListener('change', filterUsers);
    }

    if (cancelDeleteBtn) {
        cancelDeleteBtn.addEventListener('click', closeModal);
    }

    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', confirmDelete);
    }

    // Cerrar modal al hacer clic en el overlay
    if (deleteModal) {
        deleteModal.addEventListener('click', function(e) {
            if (e.target === deleteModal) {
                closeModal();
            }
        });
    }

    // Cerrar modal con ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && deleteModal && deleteModal.style.display === 'flex') {
            closeModal();
        }
    });

    // Resaltar filas al pasar el mouse
    userRows.forEach(row => {
        row.addEventListener('mouseenter', function() {
            this.classList.add('row-highlight');
        });
        row.addEventListener('mouseleave', function() {
            this.classList.remove('row-highlight');
        });
    });

    // Inicializar
    setupDeleteModal();
    setupAlertDismiss();
    checkNoResults();

    console.log('Lista de usuarios inicializada correctamente');
});