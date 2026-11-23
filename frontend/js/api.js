/* =========================================================================
 * api.js
 * Cliente HTTP del frontend. Todas las llamadas se realizan contra el API
 * Gateway. Cuando la pagina se sirve por nginx se usa la ruta relativa /api
 * (proxy reverso); cuando se abre como archivo local se apunta al gateway.
 * ========================================================================= */

const API_BASE = window.location.protocol === 'file:' ? 'http://localhost:8080/api' : '/api';

/**
 * Ejecuta una peticion HTTP y devuelve el cuerpo JSON.
 * Anade automaticamente la cabecera Authorization cuando existe token.
 * @param {string} metodo  GET | POST | PUT | PATCH | DELETE
 * @param {string} ruta    ruta relativa, por ejemplo '/productos'
 * @param {object} [cuerpo] cuerpo a enviar en JSON
 * @param {object} [opciones] { auth: false } para omitir el token
 */
async function peticion(metodo, ruta, cuerpo, opciones) {
    opciones = opciones || {};
    const cabeceras = { 'Content-Type': 'application/json' };

    const token = localStorage.getItem(Util.CLAVES.token);
    if (token && opciones.auth !== false) {
        cabeceras['Authorization'] = 'Bearer ' + token;
    }

    const configuracion = { method: metodo, headers: cabeceras };
    if (cuerpo !== undefined && cuerpo !== null) {
        configuracion.body = JSON.stringify(cuerpo);
    }

    let respuesta;
    try {
        respuesta = await fetch(API_BASE + ruta, configuracion);
    } catch (error) {
        throw new Error('No se pudo conectar con el servidor. Verifique que el API Gateway esté en ejecución.');
    }

    let datos = null;
    if (respuesta.status !== 204) {
        const texto = await respuesta.text();
        if (texto) {
            try {
                datos = JSON.parse(texto);
            } catch (error) {
                datos = texto;
            }
        }
    }

    if (!respuesta.ok) {
        throw new Error(Util.mensajeDeError(datos, respuesta.status));
    }

    return datos;
}

const API = {
    base: API_BASE,
    get: (ruta, opciones) => peticion('GET', ruta, null, opciones),
    post: (ruta, cuerpo, opciones) => peticion('POST', ruta, cuerpo, opciones),
    put: (ruta, cuerpo, opciones) => peticion('PUT', ruta, cuerpo, opciones),
    patch: (ruta, cuerpo, opciones) => peticion('PATCH', ruta, cuerpo, opciones),
    del: (ruta, opciones) => peticion('DELETE', ruta, null, opciones)
};

/* =========================================================================
 * Util
 * Funciones de apoyo reutilizadas por todas las paginas: claves de
 * almacenamiento, formato, construccion de tarjetas de producto,
 * notificaciones y lectura de mensajes de error del backend.
 * ========================================================================= */

const Util = {
    CLAVES: {
        token: 'tv_token',
        idUsuario: 'tv_idUsuario',
        nombre: 'tv_nombre',
        correo: 'tv_correo',
        rol: 'tv_rol'
    },

    /** Extrae un mensaje legible de la respuesta de error del backend. */
    mensajeDeError(datos, estado) {
        if (datos && typeof datos === 'object') {
            if (Array.isArray(datos.detalles) && datos.detalles.length > 0) {
                return datos.detalles.join('. ');
            }
            if (datos.mensaje) return datos.mensaje;
            if (datos.error) return datos.error;
            if (datos.message) return datos.message;
        }
        if (typeof datos === 'string' && datos.trim()) {
            return datos;
        }
        if (estado === 400) return 'Los datos enviados no son válidos.';
        if (estado === 401) return 'Su sesión no es válida o ha expirado. Inicie sesión nuevamente.';
        if (estado === 403) return 'No tiene permisos para realizar esta acción.';
        if (estado === 404) return 'El recurso solicitado no existe.';
        if (estado === 409) return 'La operación entra en conflicto con los datos actuales.';
        if (estado >= 500) return 'Ocurrió un error en el servidor. Intente nuevamente.';
        return 'Ocurrió un error al procesar la solicitud (código ' + estado + ').';
    },

    escapar(texto) {
        if (texto === null || texto === undefined) return '';
        return String(texto)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    },

    formatearPrecio(valor) {
        const numero = Number(valor);
        if (isNaN(numero)) return 'S/ 0.00';
        return 'S/ ' + numero.toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    },

    formatearFecha(valor) {
        if (!valor) return '—';
        const fecha = new Date(valor);
        if (isNaN(fecha.getTime())) return String(valor);
        return fecha.toLocaleString('es-PE', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },

    /** Devuelve el bloque de imagen de un producto (con marcador de posición). */
    imagenProducto(producto) {
        const tieneImagen = producto.imagen && String(producto.imagen).trim();
        let html = '<div class="producto-imagen">';
        html += '<span class="placeholder-icon"><i class="bi bi-image"></i></span>';
        if (tieneImagen) {
            html += '<img src="' + Util.escapar(producto.imagen) + '" alt="' + Util.escapar(producto.nombre) +
                    '" loading="lazy" onerror="this.style.display=\'none\'">';
        }
        if (producto.categoria) {
            html += '<span class="producto-categoria">' + Util.escapar(producto.categoria) + '</span>';
        }
        html += '</div>';
        return html;
    },

    /** Miniatura cuadrada para tablas y listas. */
    miniaturaProducto(producto) {
        const tieneImagen = producto.imagen && String(producto.imagen).trim();
        let html = '<div class="miniatura-producto">';
        html += '<span class="placeholder-icon"><i class="bi bi-image"></i></span>';
        if (tieneImagen) {
            html += '<img src="' + Util.escapar(producto.imagen) + '" alt="' + Util.escapar(producto.nombre) +
                    '" loading="lazy" onerror="this.style.display=\'none\'">';
        }
        html += '</div>';
        return html;
    },

    /** Tarjeta de producto en formato columna de grid de Bootstrap. */
    tarjetaProducto(producto) {
        const stock = Number(producto.stock);
        const agotado = isNaN(stock) || stock <= 0;
        let html = '<div class="col">';
        html += '<article class="card producto-card border-0 shadow-sm">';
        html += Util.imagenProducto(producto);
        html += '<div class="card-body d-flex flex-column">';
        html += '<h3 class="producto-nombre h6 card-title">' + Util.escapar(producto.nombre) + '</h3>';
        html += '<p class="producto-descripcion card-text small text-muted flex-grow-1">' +
                Util.escapar(producto.descripcion || 'Sin descripción disponible.') + '</p>';
        html += '<div class="d-flex justify-content-between align-items-center mb-2">';
        html += '<span class="producto-precio">' + Util.formatearPrecio(producto.precio) + '</span>';
        html += '<span class="badge ' + (agotado ? 'text-bg-danger' : 'text-bg-success') + '">' +
                (agotado ? 'Agotado' : 'Stock: ' + Util.escapar(stock)) + '</span>';
        html += '</div>';
        html += '<button type="button" class="btn btn-primary w-100 js-agregar-carrito" data-id="' + producto.idProducto + '"' +
                (agotado ? ' disabled' : '') + '>';
        html += '<i class="bi ' + (agotado ? 'bi-x-circle' : 'bi-cart-plus') + ' me-1"></i>' +
                (agotado ? 'Sin stock' : 'Agregar al carrito');
        html += '</button>';
        html += '</div></article></div>';
        return html;
    },

    /** Notificacion flotante estilizada con Bootstrap Toast. */
    notificar(mensaje, tipo) {
        tipo = tipo || 'success';
        let contenedor = document.getElementById('tv-toast-container');
        if (!contenedor) {
            contenedor = document.createElement('div');
            contenedor.id = 'tv-toast-container';
            contenedor.className = 'toast-container position-fixed top-0 end-0 p-3';
            document.body.appendChild(contenedor);
        }
        const iconos = {
            success: 'bi-check-circle-fill',
            danger: 'bi-x-circle-fill',
            warning: 'bi-exclamation-triangle-fill',
            info: 'bi-info-circle-fill'
        };
        const icono = iconos[tipo] || iconos.info;
        const id = 'toast-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
        const html =
            '<div id="' + id + '" class="toast align-items-center text-bg-' + tipo + ' border-0" role="alert" aria-live="assertive" aria-atomic="true">' +
                '<div class="d-flex">' +
                    '<div class="toast-body"><i class="bi ' + icono + ' me-2"></i>' + Util.escapar(mensaje) + '</div>' +
                    '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Cerrar"></button>' +
                '</div>' +
            '</div>';
        contenedor.insertAdjacentHTML('beforeend', html);
        const elemento = document.getElementById(id);
        const toast = new bootstrap.Toast(elemento, { delay: 4200 });
        toast.show();
        elemento.addEventListener('hidden.bs.toast', () => elemento.remove());
    },

    /** Muestra un indicador de carga dentro de un contenedor. */
    mostrarCargando(contenedor, texto) {
        if (!contenedor) return;
        contenedor.innerHTML = '<div class="cargando w-100"><div class="spinner-border text-primary mb-2" role="status"></div>' +
            '<div>' + Util.escapar(texto || 'Cargando...') + '</div></div>';
    },

    /** Estado vacio reutilizable. */
    estadoVacio(icono, mensaje) {
        return '<div class="estado-vacio"><i class="bi ' + icono + '"></i>' +
            '<p class="mb-0">' + Util.escapar(mensaje) + '</p></div>';
    }
};
