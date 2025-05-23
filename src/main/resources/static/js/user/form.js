
document.addEventListener('DOMContentLoaded', function() {

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const confirmPasswordInput = document.getElementById('confirmPassword');
    const usernameMessage = document.getElementById('usernameMessage');
    const passwordMessage = document.getElementById('passwordMessage');

    // Variables para controlar la validación
    let isUsernameValid = false;
    let isPasswordValid = false;
    let isCheckingUsername = false;

    // Obtener configuración del servidor (si existe el usuario actual)
    const userIdInput = document.getElementById('id');
    const isEditing = userIdInput && userIdInput.value;
    const originalUsername = isEditing ? usernameInput.value : '';

    // Verificar disponibilidad de username
    if (usernameInput) {
        usernameInput.addEventListener('blur', function() {
            const username = this.value.trim();

            // Limpiar mensaje anterior
            clearUsernameMessage();

            if (username.length < 4) {
                showUsernameMessage('El nombre de usuario debe tener al menos 4 caracteres', 'error');
                isUsernameValid = false;
                return;
            }

            // Si estamos editando y el username no cambió, es válido
            if (isEditing && username === originalUsername) {
                showUsernameMessage('Usuario actual', 'success');
                isUsernameValid = true;
                return;
            }

            // Verificar disponibilidad con el servidor
            checkUsernameAvailability(username);
        });
    }

    // Validar que las contraseñas coincidan
    if (confirmPasswordInput) {
        confirmPasswordInput.addEventListener('input', function() {
            const password = passwordInput.value;
            const confirmPassword = this.value;

            if (confirmPassword === '') {
                clearPasswordMessage();
                isPasswordValid = false;
                return;
            }

            if (password === confirmPassword) {
                showPasswordMessage('Las contraseñas coinciden', 'success');
                isPasswordValid = true;
            } else {
                showPasswordMessage('Las contraseñas no coinciden', 'error');
                isPasswordValid = false;
            }
        });
    }

    // También validar cuando se escriba en el campo de contraseña
    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            if (confirmPasswordInput && confirmPasswordInput.value) {
                // Triggear la validación del campo de confirmación
                confirmPasswordInput.dispatchEvent(new Event('input'));
            }
        });
    }

    // Funciones para mostrar mensajes
    function showUsernameMessage(message, type) {
        if (usernameMessage) {
            usernameMessage.className = 'username-feedback ' + type;
            usernameMessage.innerHTML = message;
            usernameMessage.style.display = 'block';
        }
    }

    function showPasswordMessage(message, type) {
        if (passwordMessage) {
            passwordMessage.className = 'password-feedback ' + type;
            passwordMessage.innerHTML = message;
            passwordMessage.style.display = 'block';
        }
    }

    function clearUsernameMessage() {
        if (usernameMessage) {
            usernameMessage.innerHTML = '';
            usernameMessage.style.display = 'none';
        }
    }

    function clearPasswordMessage() {
        if (passwordMessage) {
            passwordMessage.innerHTML = '';
            passwordMessage.style.display = 'none';
        }
    }

    // Verificar disponibilidad del username con el servidor
    function checkUsernameAvailability(username) {
        isCheckingUsername = true;
        showUsernameMessage('Verificando disponibilidad...', 'checking');

        // Construir URL con parámetros
        let url = '/users/api/check-username?username=' + encodeURIComponent(username);

        // Si estamos editando, agregar el ID del usuario
        if (isEditing) {
            url += '&userId=' + userIdInput.value;
        }

        // Hacer petición al servidor
        fetch(url)
            .then(response => response.json())
            .then(data => {
                isCheckingUsername = false;

                if (data.available) {
                    showUsernameMessage('Usuario disponible', 'success');
                    isUsernameValid = true;
                } else {
                    showUsernameMessage('El nombre de usuario ya está en uso', 'error');
                    isUsernameValid = false;
                }
            })
            .catch(error => {
                console.error('Error verificando username:', error);
                isCheckingUsername = false;
                showUsernameMessage('Error verificando disponibilidad', 'error');
                isUsernameValid = false;
            });
    }

    // Validación del formulario antes de enviar
    const form = document.querySelector('.user-form');
    if (form) {
        form.addEventListener('submit', function(e) {
            // Si estamos verificando username, esperar
            if (isCheckingUsername) {
                e.preventDefault();
                alert('Espere mientras se verifica la disponibilidad del usuario');
                return;
            }

            // Validaciones solo para modo creación
            if (!isEditing) {
                // Verificar que el username sea válido
                if (!isUsernameValid) {
                    e.preventDefault();
                    alert('Por favor ingrese un nombre de usuario válido y disponible');
                    usernameInput.focus();
                    return;
                }

                // Verificar que las contraseñas coincidan
                if (!isPasswordValid) {
                    e.preventDefault();
                    alert('Las contraseñas deben coincidir');
                    confirmPasswordInput.focus();
                    return;
                }
            }

            // Validaciones generales
            const requiredFields = ['name', 'lastName', 'username', 'role'];
            for (let fieldName of requiredFields) {
                const field = document.getElementById(fieldName);
                if (field && !field.value.trim()) {
                    e.preventDefault();
                    alert('Por favor complete todos los campos obligatorios');
                    field.focus();
                    return;
                }
            }
        });
    }

    // Validación inicial si estamos editando
    if (isEditing && usernameInput.value) {
        isUsernameValid = true;
        showUsernameMessage('Usuario actual', 'success');
    }

    // Si estamos editando, las contraseñas no son obligatorias
    if (isEditing) {
        isPasswordValid = true;
    }
});