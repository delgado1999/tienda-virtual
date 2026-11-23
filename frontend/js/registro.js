/* =========================================================================
 * registro.js
 * Registro de usuarios. Valida los campos y llama a POST /usuarios.
 * Tras el registro exitoso redirige a login.html.
 * ========================================================================= */

function mostrarErrorRegistro(mensaje) {
    const alerta = document.getElementById('registro-error');
    if (!alerta) return;
    alerta.textContent = mensaje;
    alerta.classList.remove('d-none');
}

function limpiarErroresRegistro() {
    const alerta = document.getElementById('registro-error');
    if (alerta) alerta.classList.add('d-none');
    ['error-reg-nombre', 'error-reg-correo', 'error-reg-password'].forEach(id => {
        const elemento = document.getElementById(id);
        if (elemento) elemento.textContent = '';
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const formulario = document.getElementById('form-registro');
    if (!formulario) return;

    formulario.addEventListener('submit', async evento => {
        evento.preventDefault();
        limpiarErroresRegistro();

        const nombre = document.getElementById('reg-nombre').value.trim();
        const correo = document.getElementById('reg-correo').value.trim();
        const password = document.getElementById('reg-password').value;
        const direccion = document.getElementById('reg-direccion').value.trim();
        const telefono = document.getElementById('reg-telefono').value.trim();

        let valido = true;
        if (!nombre) {
            document.getElementById('error-reg-nombre').textContent = 'El nombre es obligatorio.';
            valido = false;
        }
        if (!correo) {
            document.getElementById('error-reg-correo').textContent = 'El correo es obligatorio.';
            valido = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo)) {
            document.getElementById('error-reg-correo').textContent = 'Ingrese un correo válido.';
            valido = false;
        }
        if (!password) {
            document.getElementById('error-reg-password').textContent = 'La contraseña es obligatoria.';
            valido = false;
        } else if (password.length < 6) {
            document.getElementById('error-reg-password').textContent = 'La contraseña debe tener al menos 6 caracteres.';
            valido = false;
        }
        if (!valido) return;

        const boton = document.getElementById('btn-registro');
        boton.disabled = true;
        const contenidoOriginal = boton.innerHTML;
        boton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Registrando...';

        try {
            const nuevo = await API.post('/usuarios', {
                nombre: nombre,
                correo: correo,
                password: password,
                direccion: direccion,
                telefono: telefono
            }, { auth: false });

            const exito = document.getElementById('registro-exito');
            if (exito) {
                exito.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Cuenta creada correctamente para <strong>' +
                    Util.escapar(nuevo.correo || correo) + '</strong>. Redirigiendo al inicio de sesión...';
                exito.classList.remove('d-none');
            }
            Util.notificar('Registro exitoso. Ya puedes iniciar sesión.', 'success');
            setTimeout(() => { window.location.href = 'login.html'; }, 1600);
        } catch (error) {
            mostrarErrorRegistro(error.message);
            Util.notificar(error.message, 'danger');
            boton.disabled = false;
            boton.innerHTML = contenidoOriginal;
        }
    });
});
