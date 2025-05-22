// JavaScript para validación de contraseñas
document.addEventListener('DOMContentLoaded', function() {
    const newPasswordInput = document.getElementById('newPassword');
    const confirmPasswordInput = document.getElementById('confirmNewPassword');
    const passwordMessage = document.getElementById('passwordMessage');
    const changePasswordBtn = document.getElementById('changePasswordBtn');

    let isPasswordValid = false;

    // Validar que las contraseñas coincidan
    function validatePasswords() {
        const newPassword = newPasswordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        if (confirmPassword === '') {
            clearPasswordMessage();
            isPasswordValid = false;
            updateButton();
            return;
        }

        if (newPassword.length < 6) {
            showPasswordMessage('La contraseña debe tener al menos 6 caracteres', 'error');
            isPasswordValid = false;
            updateButton();
            return;
        }

        if (newPassword === confirmPassword) {
            showPasswordMessage('Las contraseñas coinciden', 'success');
            isPasswordValid = true;
        } else {
            showPasswordMessage('Las contraseñas no coinciden', 'error');
            isPasswordValid = false;
        }
        updateButton();
    }

    // Event listeners
    if (newPasswordInput) {
        newPasswordInput.addEventListener('input', validatePasswords);
    }

    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', validatePasswords);
    }

    // Funciones auxiliares
    function showPasswordMessage(message, type) {
        if (passwordMessage) {
            passwordMessage.className = 'password-feedback ' + type;
            passwordMessage.innerHTML = message;
            passwordMessage.style.display = 'block';
        }
    }

    function clearPasswordMessage() {
        if (passwordMessage) {
            passwordMessage.innerHTML = '';
            passwordMessage.style.display = 'none';
        }
    }

    function updateButton() {
        if (changePasswordBtn) {
            changePasswordBtn.disabled = !isPasswordValid;
        }
    }

    // Validación del formulario
    const passwordForm = document.querySelector('.password-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            if (!isPasswordValid) {
                e.preventDefault();
                alert('Por favor complete todos los campos correctamente');
                return;
            }

            // Confirmación antes de cambiar
            if (!confirm('¿Está seguro de que desea cambiar su contraseña?')) {
                e.preventDefault();
            }
        });
    }

    // Función para limpiar formulario
    window.clearPasswordForm = function() {
        if (passwordForm) {
            passwordForm.reset();
            clearPasswordMessage();
            isPasswordValid = false;
            updateButton();
        }
    };

    // Auto-dismiss para alertas
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            alert.style.transform = 'translateY(-10px)';
            setTimeout(() => alert.remove(), 300);
        }, 5000);
    });
});