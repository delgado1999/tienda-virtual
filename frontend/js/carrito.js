/* =========================================================================
 * carrito.js
 * Contiene dos responsabilidades:
 *   1) El modulo Carrito: almacenamiento del carrito en localStorage y el
 *      contador de la navbar. Se carga en TODAS las paginas.
 *   2) La logica de la pagina carrito.html (pintado, validacion de stock y
 *      confirmacion del pedido), que solo se ejecuta si existe su marcado.
 * ========================================================================= */

const Carrito = {
    CLAVE: 'carrito_tienda',

    leer() {
        try {
            const crudo = localStorage.getItem(Carrito.CLAVE);
            const datos = crudo ? JSON.parse(crudo) : [];
            return Array.isArray(datos) ? datos : [];
        } catch (error) {
            return [];
        }
    },

    guardar(items) {
        localStorage.setItem(Carrito.CLAVE, JSON.stringify(items));
        Carrito.actualizarBadge();
    },

    totalItems() {
        return Carrito.leer().reduce((total, item) => total + Number(item.cantidad || 0), 0);
    },

    totalPrecio() {
        return Carrito.leer().reduce((total, item) => total + Number(item.precio || 0) * Number(item.cantidad || 0), 0);
    },

    buscar(idProducto) {
        return Carrito.leer().find(item => Number(item.idProducto) === Number(idProducto));
    },

    agregar(producto, cantidad) {
        cantidad = cantidad || 1;
        const stock = Number(producto.stock || 0);
        if (stock <= 0) {
            Util.notificar('El producto "' + producto.nombre + '" no tiene stock disponible.', 'warning');
            return false;
        }

        const items = Carrito.leer();
        const existente = items.find(item => Number(item.idProducto) === Number(producto.idProducto));
        const cantidadActual = existente ? Number(existente.cantidad) : 0;

        if (cantidadActual + cantidad > stock) {
            Util.notificar('Solo hay ' + stock + ' unidad(es) disponible(s) de "' + producto.nombre + '".', 'warning');
            return false;
        }

        if (existente) {
            existente.cantidad = cantidadActual + cantidad;
        } else {
            items.push({
                idProducto: Number(producto.idProducto),
                nombre: producto.nombre,
                precio: Number(producto.precio),
                imagen: producto.imagen || '',
                stock: stock,
                cantidad: cantidad
            });
        }

        Carrito.guardar(items);
        Util.notificar('"' + producto.nombre + '" se agregó al carrito.', 'success');
        return true;
    },

    quitar(idProducto) {
        const items = Carrito.leer().filter(item => Number(item.idProducto) !== Number(idProducto));
        Carrito.guardar(items);
    },

    fijarCantidad(idProducto, cantidad) {
        const items = Carrito.leer();
        const item = items.find(i => Number(i.idProducto) === Number(idProducto));
        if (!item) return;
        let nueva = Number(cantidad);
        if (isNaN(nueva) || nueva < 1) nueva = 1;
        if (nueva > Number(item.stock)) nueva = Number(item.stock);
        item.cantidad = nueva;
        Carrito.guardar(items);
    },

    vaciar() {
        localStorage.removeItem(Carrito.CLAVE);
        Carrito.actualizarBadge();
    },

    actualizarBadge() {
        const total = Carrito.totalItems();
        document.querySelectorAll('.badge-carrito').forEach(badge => {
            badge.textContent = total;
        });
    }
};

/* -------------------------------------------------------------------------
 * Pagina carrito.html
 * ------------------------------------------------------------------------- */

let productosCarrito = [];

async function validarStockCarrito() {
    const items = Carrito.leer();
    if (items.length === 0) {
        productosCarrito = [];
        return;
    }

    const validados = [];
    for (const item of items) {
        try {
            const producto = await API.get('/productos/' + item.idProducto);
            const stock = Number(producto.stock);
            if (stock <= 0) {
                Util.notificar('"' + producto.nombre + '" ya no tiene stock y fue retirado del carrito.', 'warning');
                continue;
            }
            const entrada = {
                idProducto: producto.idProducto,
                nombre: producto.nombre,
                precio: Number(producto.precio),
                imagen: producto.imagen || '',
                stock: stock,
                cantidad: Number(item.cantidad)
            };
            if (entrada.cantidad > stock) {
                entrada.cantidad = stock;
                Util.notificar('Se ajustó la cantidad de "' + producto.nombre + '" al stock disponible (' + stock + ').', 'info');
            }
            validados.push(entrada);
        } catch (error) {
            Util.notificar('Un producto ya no está disponible y fue retirado del carrito.', 'warning');
        }
    }

    Carrito.guardar(validados);
}

function pintarCarrito() {
    const cuerpo = document.getElementById('cuerpo-carrito');
    const vacio = document.getElementById('carrito-vacio');
    const contenido = document.getElementById('carrito-contenido');
    if (!cuerpo) return;

    const items = Carrito.leer();
    productosCarrito = items;

    if (items.length === 0) {
        cuerpo.innerHTML = '';
        if (vacio) vacio.classList.remove('d-none');
        if (contenido) contenido.classList.add('d-none');
        return;
    }

    if (vacio) vacio.classList.add('d-none');
    if (contenido) contenido.classList.remove('d-none');

    let html = '';
    items.forEach(item => {
        const subtotal = Number(item.precio) * Number(item.cantidad);
        html += '<tr>';
        html += '<td><div class="d-flex align-items-center gap-3">' + Util.miniaturaProducto(item) +
                '<div><div class="fw-semibold">' + Util.escapar(item.nombre) + '</div>' +
                '<div class="small text-muted">Stock disponible: ' + Util.escapar(item.stock) + '</div></div></div></td>';
        html += '<td class="text-nowrap">' + Util.formatearPrecio(item.precio) + '</td>';
        html += '<td><div class="input-group input-group-sm control-cantidad">' +
                    '<button class="btn btn-outline-secondary js-menos" type="button" data-id="' + item.idProducto + '" aria-label="Quitar una unidad"><i class="bi bi-dash"></i></button>' +
                    '<input type="number" class="form-control text-center js-cantidad" data-id="' + item.idProducto + '" value="' + Number(item.cantidad) + '" min="1" max="' + Number(item.stock) + '" aria-label="Cantidad">' +
                    '<button class="btn btn-outline-secondary js-mas" type="button" data-id="' + item.idProducto + '" aria-label="Agregar una unidad"><i class="bi bi-plus"></i></button>' +
                '</div></td>';
        html += '<td class="text-nowrap fw-semibold">' + Util.formatearPrecio(subtotal) + '</td>';
        html += '<td class="text-end"><button class="btn btn-outline-danger btn-sm js-eliminar" type="button" data-id="' + item.idProducto + '" aria-label="Eliminar producto"><i class="bi bi-trash"></i></button></td>';
        html += '</tr>';
    });
    cuerpo.innerHTML = html;

    const total = document.getElementById('total-carrito');
    if (total) total.textContent = Util.formatearPrecio(Carrito.totalPrecio());

    const cantidad = document.getElementById('cantidad-articulos');
    if (cantidad) cantidad.textContent = Carrito.totalItems();
}

function manejarClicCarrito(evento) {
    const boton = evento.target.closest('button');
    if (!boton) return;
    const id = Number(boton.dataset.id);

    if (boton.classList.contains('js-menos')) {
        const item = Carrito.buscar(id);
        if (item) Carrito.fijarCantidad(id, Number(item.cantidad) - 1);
        pintarCarrito();
    } else if (boton.classList.contains('js-mas')) {
        const item = Carrito.buscar(id);
        if (item) {
            if (Number(item.cantidad) + 1 > Number(item.stock)) {
                Util.notificar('No hay más stock disponible de este producto.', 'warning');
            } else {
                Carrito.fijarCantidad(id, Number(item.cantidad) + 1);
            }
        }
        pintarCarrito();
    } else if (boton.classList.contains('js-eliminar')) {
        Carrito.quitar(id);
        Util.notificar('Producto eliminado del carrito.', 'info');
        pintarCarrito();
    }
}

function manejarCambioCarrito(evento) {
    const entrada = evento.target.closest('.js-cantidad');
    if (!entrada) return;
    Carrito.fijarCantidad(Number(entrada.dataset.id), entrada.value);
    pintarCarrito();
}

function mostrarExitoPedido(pedido) {
    const numero = document.getElementById('pedido-numero');
    const total = document.getElementById('pedido-total');
    const estado = document.getElementById('pedido-estado');
    if (numero) numero.textContent = '#' + pedido.idPedido;
    if (total) total.textContent = Util.formatearPrecio(pedido.total);
    if (estado) estado.textContent = pedido.estado;

    const elemento = document.getElementById('modal-pedido-exito');
    if (elemento) {
        bootstrap.Modal.getOrCreateInstance(elemento).show();
    } else {
        Util.notificar('Pedido #' + pedido.idPedido + ' registrado. Total: ' + Util.formatearPrecio(pedido.total), 'success');
    }
}

async function confirmarPedido() {
    if (Carrito.leer().length === 0) {
        Util.notificar('Su carrito está vacío.', 'warning');
        return;
    }

    if (!Auth.estaAutenticado()) {
        Util.notificar('Debe iniciar sesión para confirmar el pedido. Redirigiendo...', 'info');
        setTimeout(() => { window.location.href = 'login.html'; }, 1200);
        return;
    }

    const boton = document.getElementById('btn-confirmar-pedido');
    if (boton) boton.disabled = true;

    try {
        await validarStockCarrito();
        pintarCarrito();
        const items = Carrito.leer();
        if (items.length === 0) {
            return;
        }

        const pedido = await API.post('/ventas', {
            idUsuario: Auth.idUsuario(),
            items: items.map(item => ({ idProducto: item.idProducto, cantidad: item.cantidad }))
        });

        Carrito.vaciar();
        pintarCarrito();
        mostrarExitoPedido(pedido);
    } catch (error) {
        Util.notificar(error.message, 'danger');
    } finally {
        if (boton) boton.disabled = false;
    }
}

async function iniciarPaginaCarrito() {
    const cuerpo = document.getElementById('cuerpo-carrito');
    if (!cuerpo) return;

    if (Carrito.leer().length > 0) {
        cuerpo.innerHTML = '<tr><td colspan="5" class="text-center py-4">' +
            '<div class="spinner-border text-primary mb-2" role="status"></div>' +
            '<div class="text-muted">Verificando disponibilidad de stock...</div></td></tr>';
    }

    await validarStockCarrito();
    pintarCarrito();

    cuerpo.addEventListener('click', manejarClicCarrito);
    cuerpo.addEventListener('change', manejarCambioCarrito);

    const botonConfirmar = document.getElementById('btn-confirmar-pedido');
    if (botonConfirmar) botonConfirmar.addEventListener('click', confirmarPedido);

    const botonVaciar = document.getElementById('btn-vaciar-carrito');
    if (botonVaciar) {
        botonVaciar.addEventListener('click', () => {
            if (Carrito.leer().length === 0) {
                Util.notificar('El carrito ya está vacío.', 'info');
                return;
            }
            if (window.confirm('¿Desea vaciar el carrito?')) {
                Carrito.vaciar();
                pintarCarrito();
                Util.notificar('Carrito vaciado.', 'info');
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', () => {
    Carrito.actualizarBadge();
    iniciarPaginaCarrito();
});
