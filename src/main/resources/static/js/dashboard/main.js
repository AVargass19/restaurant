/**
 * Archivo principal de JavaScript para el dashboard
 * Contiene funciones generales y de inicialización
 */

// Función para cambiar el idioma
function changeLanguage() {
    const language = document.getElementById('languageSelect').value;
    window.location.href = '?lang=' + language;
}

// Renderizar el gráfico de reservas para el panel de administrador
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('reservationChart')) {
        const ctx = document.getElementById('reservationChart').getContext('2d');
        const reservationChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'],
                datasets: [{
                    label: 'Reservas',
                    data: [5, 7, 10, 12, 18, 25, 20],
                    backgroundColor: '#8C1C13',
                    borderColor: '#8C1C13',
                    tension: 0.2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false
            }
        });
    }
});