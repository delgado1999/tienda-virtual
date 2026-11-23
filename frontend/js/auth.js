/* =========================================================================
 * auth.js
 * Gestion de la sesion del usuario en localStorage, proteccion de paginas
 * y pintado del estado del usuario en la barra de navegacion.
 * ========================================================================= */

const Auth = {
    guardarSesion(respuesta) {
        localStorage.setItem(Util.CLAVES.token, respuesta.token || '');
        localStorage.setItem(Util.CLAVES.idUsuario, respuesta.idUsuario != null ? respuesta.idUsuario : '');
        localStorage.setItem(Util.CLAVES.nombre, respuesta.nombre || '');
        localStorage.setItem(Util.CLAVES.correo, respuesta.correo || '');
        localStorage.setItem(Util.CLAVES.rol, respuesta.rol || 'CLIENTE');
    },

    token() { return localStorage.getItem(Util.CLAVES.token); },
    nombre() { return localStorage.getItem(Util.CLAVES.nombre) || ''; },
    correo() { return localStorage.getItem(Util.CLAVES.correo) || ''; },
    rol() { return localStorage.getItem(Util.CLAVES.rol) || ''; },

    idUsuario() {
        const valor = localStorage.getItem(Util.CLAVES.idUsuario);
        return valor ? Number(valor) : null;
    },

    estaAutenticado() {
        const token = localStorage.getItem(Util.CLAVES.token);
        return !!(token && token.trim());
    },

    esAdmin() {
        return localStorage.getItem(Util.CLAVES.rol) === 'ADMIN';
    },

    cerrarSesion() {
        Object.keys(Util.CLAVES).forEach(clave => localStorage.removeItem(Util.CLAVES[clave]));
        window.location.href = 'index.html';
    },

    /**
     * Protege la pagina actual. Si no hay sesion redirige al login y si se
     * exige rol de administrador y el usuario no lo tiene, vuelve al inicio.
     */
    protegerPagina(opciones) {
        opciones = opciones || {};
        if (!Auth.estaAutenticado()) {
            window.location.href = 'login.html';
            return false;
        }
        if (opciones.admin && !Auth.esAdmin()) {
            window.location.href = 'index.html';
            return false;
        }
        return true;
    },

    /** Rellena la zona de usuario de la navbar y muestra el enlace de admin. */
    pintarNavbar() {
        const enlaceAdmin = document.getElementById('enlace-admin');
        if (enlaceAdmin) {
            enlaceAdmin.classList.toggle('d-none', !Auth.esAdmin());
        }

        const zona = document.getElementById('zona-usuario');
        if (!zona) return;

        if (Auth.estaAutenticado()) {
            let html = '<li class="nav-item dropdown">';
            html += '<a class="nav-link dropdown-toggle d-flex align-items-center gap-2" href="#" id="menuUsuario" role="button" data-bs-toggle="dropdown" aria-expanded="false">';
            html += '<i class="bi bi-person-circle fs-5"></i>';
            html += '<span class="nombre-usuario">' + Util.escapar(Auth.nombre() || 'Usuario') + '</span>';
            if (Auth.esAdmin()) {
                html += '<span class="badge text-bg-warning text-dark">ADMIN</span>';
            }
            html += '</a>';
            html += '<ul class="dropdown-menu dropdown-menu-end" aria-labelledby="menuUsuario">';
            html += '<li><span class="dropdown-item-text small text-muted">' + Util.escapar(Auth.correo()) + '</span></li>';
            html += '<li><hr class="dropdown-divider"></li>';
            html += '<li><a class="dropdown-item" href="carrito.html"><i class="bi bi-bag me-2"></i>Mi carrito</a></li>';
            if (Auth.esAdmin()) {
                html += '<li><a class="dropdown-item" href="admin.html"><i class="bi bi-speedometer2 me-2"></i>Panel Admin</a></li>';
            }
            html += '<li><hr class="dropdown-divider"></li>';
            html += '<li><button type="button" class="dropdown-item text-danger" id="btn-cerrar-sesion">';
            html += '<i class="bi bi-box-arrow-right me-2"></i>Cerrar sesión</button></li>';
            html += '</ul></li>';
            zona.innerHTML = html;

            const boton = document.getElementById('btn-cerrar-sesion');
            if (boton) {
                boton.addEventListener('click', () => Auth.cerrarSesion());
            }
        } else {
            zona.innerHTML =
                '<li class="nav-item"><a class="nav-link" href="login.html">' +
                    '<i class="bi bi-box-arrow-in-right me-1"></i>Iniciar sesión</a></li>' +
                '<li class="nav-item"><a class="btn btn-primary ms-lg-2" href="registro.html">' +
                    '<i class="bi bi-person-plus me-1"></i>Registrarse</a></li>';
        }
    }
};

document.addEventListener('DOMContentLoaded', () => {
    Auth.pintarNavbar();

    const anio = document.getElementById('anio-actual');
    if (anio) {
        anio.textContent = new Date().getFullYear();
    }

    const proteccion = document.body.getAttribute('data-proteccion');
    if (proteccion === 'auth') {
        Auth.protegerPagina();
    } else if (proteccion === 'admin') {
        Auth.protegerPagina({ admin: true });
    }
});
