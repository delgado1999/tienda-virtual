/* =========================================================================
 * login.js
 * Formulario de inicio de sesion. Guarda la sesion en localStorage y
 * redirige segun el rol (ADMIN -> admin.html, CLIENTE -> index.html).
 * ========================================================================= */

function mostrarErrorLogin(mensaje) {
    const alerta = document.getElementById('login-error');
    if (!alerta) return;
    alerta.textContent = mensaje;
    alerta.classList.remove('d-none');
}

function ocultarErrorLogin() {
    const alerta = document.getElementById('login-error');
    if (alerta) alerta.classList.add('d-none');
}

document.addEventListener('DOMContentLoaded', () => {
    const formulario = document.getElementById('form-login');
    if (!formulario) return;

    // Si ya hay una sesion activa, se informa al usuario.
    if (Auth.estaAutenticado()) {
        const informacion = document.getElementById('login-info');
        if (informacion) {
            informacion.innerHTML = 'Ya tienes una sesión iniciada como <strong>' +
                Util.escapar(Auth.nombre()) + '</strong>. Puedes ir al ' +
                '<a href="' + (Auth.esAdmin() ? 'admin.html' : 'index.html') + '">panel</a> o cerrar sesión para cambiar de usuario.';
            informacion.classList.remove('d-none');
        }
    }

    // Mostrar u ocultar la contrasena.
    const botonVer = document.getElementById('btn-ver-password');
    if (botonVer) {
        botonVer.addEventListener('click', () => {
            const entrada = document.getElementById('login-password');
            const icono = document.getElementById('icono-ver-password');
            if (!entrada) return;
            const visible = entrada.type === 'text';
            entrada.type = visible ? 'password' : 'text';
            if (icono) icono.className = visible ? 'bi bi-eye' : 'bi bi-eye-slash';
        });
    }

    formulario.addEventListener('submit', async evento => {
        evento.preventDefault();
        ocultarErrorLogin();

        const correo = document.getElementById('login-correo').value.trim();
        const password = document.getElementById('login-password').value;
        const errorCorreo = document.getElementById('error-login-correo');
        const errorPassword = document.getElementById('error-login-password');
        errorCorreo.textContent = '';
        errorPassword.textContent = '';

        let valido = true;
        if (!correo) {
            errorCorreo.textContent = 'El correo es obligatorio.';
            valido = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
            errorCorreo.textContent = 'Ingrese un correo válido.';
            valido = false;
        }
        if (!password) {
            errorPassword.textContent = 'La contraseña es obligatoria.';
            valido = false;
        }
        if (!valido) return;

        const boton = document.getElementById('btn-login');
        boton.disabled = true;
        const contenidoOriginal = boton.innerHTML;
        boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Ingresando...';

        try {
            const respuesta = await API.post('/auth/login', { correo: correo, password: password }, { auth: false });
            Auth.guardarSesion(respuesta);
            Util.notificar('Bienvenido, ' + (respuesta.nombre || 'usuario') + '.', 'success');
            const destino = respuesta.rol === 'ADMIN' ? 'admin.html' : 'index.html';
            setTimeout(() => { window.location.href = destino; }, 600);
        } catch (error) {
            mostrarErrorLogin(error.message);
            Util.notificar(error.message, 'danger');
            boton.disabled = false;
            boton.innerHTML = contenidoOriginal;
        }
    });
});
