/* =========================================================================
 * admin.js
 * Panel administrativo: metricas del dashboard y gestion de productos,
 * pedidos, pagos y usuarios. La proteccion de acceso (rol ADMIN) se
 * controla desde auth.js mediante el atributo data-proteccion del body.
 * ========================================================================= */

const ESTADOS_PEDIDO = ['PENDIENTE', 'PAGADO', 'RECHAZADO', 'CANCELADO', 'ENTREGADO'];

let productosAdmin = [];
let pedidosAdmin = [];

/* --------------------------- Utilidades de color --------------------------- */

function claseEstadoPedido(estado) {
    switch (estado) {
        case 'PAGADO': return 'text-bg-success';
        case 'RECHAZADO': return 'text-bg-danger';
        case 'CANCELADO': return 'text-bg-secondary';
        case 'ENTREGADO': return 'text-bg-primary';
        default: return 'text-bg-warning text-dark';
    }
}

function claseEstadoPago(estado) {
    switch (estado) {
        case 'APROBADO': return 'text-bg-success';
        case 'RECHAZADO': return 'text-bg-danger';
        default: return 'text-bg-warning text-dark';
    }
}

/* --------------------------------- Métricas --------------------------------- */

async function cargarMetricas() {
    try {
        const [productos, usuarios, pedidos] = await Promise.all([
            API.get('/productos'),
            API.get('/usuarios'),
            API.get('/ventas')
        ]);
        const totalProductos = Array.isArray(productos) ? productos.length : 0;
        const totalUsuarios = Array.isArray(usuarios) ? usuarios.length : 0;
        const totalPedidos = Array.isArray(pedidos) ? pedidos.length : 0;

        document.getElementById('metrica-productos').textContent = totalProductos;
        document.getElementById('metrica-usuarios').textContent = totalUsuarios;
        document.getElementById('metrica-pedidos').textContent = totalPedidos;
    } catch (error) {
        Util.notificar('No se pudieron cargar las métricas: ' + error.message, 'warning');
    }
}

/* -------------------------------- Productos -------------------------------- */

async function cargarCategoriasAdmin() {
    try {
        const categorias = await API.get('/productos/categorias');
        const datalist = document.getElementById('lista-categorias-admin');
        if (datalist && Array.isArray(categorias)) {
            datalist.innerHTML = categorias.map(c => '<option value="' + Util.escapar(c) + '"></option>').join('');
        }
    } catch (error) {
        // El datalist es opcional: si falla, el campo sigue siendo libre.
    }
}

async function cargarProductos() {
    const cuerpo = document.getElementById('cuerpo-productos');
    const vacio = document.getElementById('productos-admin-vacio');
    if (!cuerpo) return;

    cuerpo.innerHTML = '<tr><td colspan="6" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    if (vacio) vacio.classList.add('d-none');

    try {
        const productos = await API.get('/productos');
        productosAdmin = Array.isArray(productos) ? productos : [];

        if (productosAdmin.length === 0) {
            cuerpo.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-box-seam', 'No hay productos registrados. Crea el primero con el botón "Nuevo producto".');
            }
            return;
        }

        cuerpo.innerHTML = productosAdmin.map(producto => {
            const stock = Number(producto.stock);
            const claseStock = stock <= 0 ? 'text-bg-danger' : (stock <= 5 ? 'text-bg-warning text-dark' : 'text-bg-success');
            return '<tr>' +
                '<td><div class="d-flex align-items-center gap-3 tabla-producto-info">' + Util.miniaturaProducto(producto) +
                    '<div><div class="fw-semibold">' + Util.escapar(producto.nombre) + '</div>' +
                    '<div class="small text-muted">#' + producto.idProducto + '</div></div></div></td>' +
                '<td><span class="badge text-bg-light">' + Util.escapar(producto.categoria) + '</span></td>' +
                '<td class="text-nowrap">' + Util.formatearPrecio(producto.precio) + '</td>' +
                '<td><span class="badge ' + claseStock + '">' + stock + '</span></td>' +
                '<td><span class="badge ' + (producto.estado ? 'text-bg-success' : 'text-bg-secondary') + '">' +
                    (producto.estado ? 'Activo' : 'Inactivo') + '</span></td>' +
                '<td class="text-end text-nowrap">' +
                    '<button type="button" class="btn btn-outline-secondary btn-sm js-editar" data-id="' + producto.idProducto + '" title="Editar"><i class="bi bi-pencil"></i></button> ' +
                    '<button type="button" class="btn btn-outline-primary btn-sm js-stock" data-id="' + producto.idProducto + '" title="Actualizar stock"><i class="bi bi-box-seam"></i></button> ' +
                    '<button type="button" class="btn btn-outline-danger btn-sm js-desactivar" data-id="' + producto.idProducto + '" title="Desactivar"><i class="bi bi-slash-circle"></i></button>' +
                '</td>' +
            '</tr>';
        }).join('');
    } catch (error) {
        cuerpo.innerHTML = '<tr><td colspan="6">' + Util.estadoVacio('bi-wifi-off', 'No se pudo cargar la lista de productos.') + '</td></tr>';
        Util.notificar(error.message, 'danger');
    }
}

function abrirModalProducto(producto) {
    const titulo = document.getElementById('titulo-modal-producto');
    document.getElementById('producto-id').value = producto ? producto.idProducto : '';
    document.getElementById('producto-nombre').value = producto ? producto.nombre : '';
    document.getElementById('producto-descripcion').value = producto && producto.descripcion ? producto.descripcion : '';
    document.getElementById('producto-precio').value = producto ? producto.precio : '';
    document.getElementById('producto-stock').value = producto ? producto.stock : '';
    document.getElementById('producto-imagen').value = producto && producto.imagen ? producto.imagen : '';
    document.getElementById('producto-categoria').value = producto ? producto.categoria : '';
    titulo.textContent = producto ? 'Editar producto' : 'Nuevo producto';

    bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-producto')).show();
}

async function guardarProducto(evento) {
    evento.preventDefault();
    const id = document.getElementById('producto-id').value;
    const nombre = document.getElementById('producto-nombre').value.trim();
    const descripcion = document.getElementById('producto-descripcion').value.trim();
    const precio = document.getElementById('producto-precio').value;
    const stock = document.getElementById('producto-stock').value;
    const imagen = document.getElementById('producto-imagen').value.trim();
    const categoria = document.getElementById('producto-categoria').value.trim();

    if (!nombre || !categoria) {
        Util.notificar('El nombre y la categoría son obligatorios.', 'warning');
        return;
    }
    if (!precio || Number(precio) <= 0) {
        Util.notificar('El precio debe ser mayor que cero.', 'warning');
        return;
    }
    if (stock === '' || Number(stock) < 0) {
        Util.notificar('El stock no puede ser negativo.', 'warning');
        return;
    }

    const datos = {
        nombre: nombre,
        descripcion: descripcion,
        precio: Number(precio),
        stock: Number(stock),
        imagen: imagen,
        categoria: categoria
    };

    const boton = document.getElementById('btn-guardar-producto');
    boton.disabled = true;
    try {
        if (id) {
            await API.put('/productos/' + id, datos);
            Util.notificar('Producto actualizado correctamente.', 'success');
        } else {
            await API.post('/productos', datos);
            Util.notificar('Producto creado correctamente.', 'success');
        }
        bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-producto')).hide();
        await cargarProductos();
        cargarMetricas();
        cargarCategoriasAdmin();
    } catch (error) {
        Util.notificar(error.message, 'danger');
    } finally {
        boton.disabled = false;
    }
}

function abrirModalStock(producto) {
    document.getElementById('stock-producto-id').value = producto.idProducto;
    document.getElementById('stock-producto-nombre').textContent = producto.nombre;
    document.getElementById('stock-valor').value = producto.stock;
    bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-stock')).show();
}

async function guardarStock(evento) {
    evento.preventDefault();
    const id = document.getElementById('stock-producto-id').value;
    const valor = document.getElementById('stock-valor').value;
    if (valor === '' || Number(valor) < 0) {
        Util.notificar('Ingrese un stock válido (mayor o igual a cero).', 'warning');
        return;
    }
    try {
        await API.patch('/productos/' + id + '/stock', { stock: Number(valor) });
        Util.notificar('Stock actualizado correctamente.', 'success');
        bootstrap.Modal.getOrCreateInstance(document.getElementById('modal-stock')).hide();
        await cargarProductos();
    } catch (error) {
        Util.notificar(error.message, 'danger');
    }
}

async function desactivarProducto(id) {
    const producto = productosAdmin.find(p => Number(p.idProducto) === Number(id));
    const nombre = producto ? producto.nombre : 'este producto';
    if (!window.confirm('¿Desea desactivar "' + nombre + '"? Dejará de mostrarse en la tienda.')) {
        return;
    }
    try {
        await API.del('/productos/' + id);
        Util.notificar('Producto desactivado.', 'success');
        await cargarProductos();
        cargarMetricas();
    } catch (error) {
        Util.notificar(error.message, 'danger');
    }
}

/* --------------------------------- Pedidos --------------------------------- */

async function cargarPedidos() {
    const cuerpo = document.getElementById('cuerpo-pedidos');
    const vacio = document.getElementById('pedidos-vacio');
    if (!cuerpo) return;

    cuerpo.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    if (vacio) vacio.classList.add('d-none');

    try {
        const pedidos = await API.get('/ventas');
        pedidosAdmin = Array.isArray(pedidos) ? pedidos : [];

        if (pedidosAdmin.length === 0) {
            cuerpo.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-receipt', 'Todavía no hay pedidos registrados.');
            }
            return;
        }

        cuerpo.innerHTML = pedidosAdmin.map(pedido => {
            const opciones = ESTADOS_PEDIDO.map(estado =>
                '<option value="' + estado + '"' + (estado === pedido.estado ? ' selected' : '') + '>' + estado + '</option>'
            ).join('');
            return '<tr>' +
                '<td class="fw-semibold">#' + pedido.idPedido + '</td>' +
                '<td>#' + pedido.idUsuario + '</td>' +
                '<td class="text-nowrap">' + Util.formatearPrecio(pedido.subtotal) + '</td>' +
                '<td class="fw-semibold text-nowrap">' + Util.formatearPrecio(pedido.total) + '</td>' +
                '<td class="text-nowrap small">' + Util.formatearFecha(pedido.fechaPedido) + '</td>' +
                '<td><span class="badge ' + claseEstadoPedido(pedido.estado) + '">' + Util.escapar(pedido.estado) + '</span></td>' +
                '<td><select class="form-select form-select-sm js-estado-pedido" data-id="' + pedido.idPedido + '" aria-label="Cambiar estado del pedido">' +
                    opciones + '</select></td>' +
            '</tr>';
        }).join('');
    } catch (error) {
        cuerpo.innerHTML = '<tr><td colspan="7">' + Util.estadoVacio('bi-wifi-off', 'No se pudo cargar la lista de pedidos.') + '</td></tr>';
        Util.notificar(error.message, 'danger');
    }
}

async function cambiarEstadoPedido(idPedido, estado, selector) {
    if (selector) selector.disabled = true;
    try {
        await API.patch('/ventas/' + idPedido + '/estado', { estado: estado });
        Util.notificar('Estado del pedido #' + idPedido + ' actualizado a ' + estado + '.', 'success');
        await cargarPedidos();
    } catch (error) {
        Util.notificar(error.message, 'danger');
        await cargarPedidos();
    }
}

/* ---------------------------------- Pagos ---------------------------------- */

async function buscarPagos(idPedido) {
    const cuerpo = document.getElementById('cuerpo-pagos');
    const vacio = document.getElementById('pagos-vacio');
    if (!cuerpo) return;

    if (!idPedido || Number(idPedido) <= 0) {
        Util.notificar('Ingrese un número de pedido válido.', 'warning');
        return;
    }

    cuerpo.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    if (vacio) vacio.classList.add('d-none');

    try {
        const pagos = await API.get('/pagos/pedido/' + idPedido);
        const lista = Array.isArray(pagos) ? pagos : [];

        if (lista.length === 0) {
            cuerpo.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-credit-card', 'El pedido #' + idPedido + ' no tiene pagos registrados.');
            }
            return;
        }

        cuerpo.innerHTML = lista.map(pago =>
            '<tr>' +
                '<td>#' + pago.idPago + '</td>' +
                '<td>#' + pago.idPedido + '</td>' +
                '<td class="text-nowrap">' + Util.formatearPrecio(pago.monto) + '</td>' +
                '<td>' + Util.escapar(pago.metodoPago) + '</td>' +
                '<td><span class="badge ' + claseEstadoPago(pago.estadoPago) + '">' + Util.escapar(pago.estadoPago) + '</span></td>' +
                '<td><code>' + Util.escapar(pago.codigoOperacion || '—') + '</code></td>' +
                '<td class="text-nowrap small">' + Util.formatearFecha(pago.fechaPago) + '</td>' +
            '</tr>'
        ).join('');
    } catch (error) {
        cuerpo.innerHTML = '';
        if (vacio) {
            vacio.classList.remove('d-none');
            vacio.innerHTML = Util.estadoVacio('bi-wifi-off', 'No se pudieron consultar los pagos del pedido.');
        }
        Util.notificar(error.message, 'danger');
    }
}

/* --------------------------------- Usuarios -------------------------------- */

async function cargarUsuarios() {
    const cuerpo = document.getElementById('cuerpo-usuarios');
    const vacio = document.getElementById('usuarios-vacio');
    if (!cuerpo) return;

    cuerpo.innerHTML = '<tr><td colspan="8" class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';
    if (vacio) vacio.classList.add('d-none');

    try {
        const usuarios = await API.get('/usuarios');
        const lista = Array.isArray(usuarios) ? usuarios : [];

        if (lista.length === 0) {
            cuerpo.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-people', 'No hay usuarios registrados.');
            }
            return;
        }

        cuerpo.innerHTML = lista.map(usuario =>
            '<tr>' +
                '<td>#' + usuario.idUsuario + '</td>' +
                '<td class="fw-semibold">' + Util.escapar(usuario.nombre) + '</td>' +
                '<td>' + Util.escapar(usuario.correo) + '</td>' +
                '<td class="small">' + Util.escapar(usuario.direccion || '—') + '</td>' +
                '<td class="small">' + Util.escapar(usuario.telefono || '—') + '</td>' +
                '<td><span class="badge ' + (usuario.rol === 'ADMIN' ? 'text-bg-warning text-dark' : 'text-bg-info text-dark') + '">' +
                    Util.escapar(usuario.rol) + '</span></td>' +
                '<td><span class="badge ' + (usuario.estado ? 'text-bg-success' : 'text-bg-secondary') + '">' +
                    (usuario.estado ? 'Activo' : 'Inactivo') + '</span></td>' +
                '<td class="text-nowrap small">' + Util.formatearFecha(usuario.fechaRegistro) + '</td>' +
            '</tr>'
        ).join('');
    } catch (error) {
        cuerpo.innerHTML = '<tr><td colspan="8">' + Util.estadoVacio('bi-wifi-off', 'No se pudo cargar la lista de usuarios.') + '</td></tr>';
        Util.notificar(error.message, 'danger');
    }
}

/* ---------------------------------- Inicio --------------------------------- */

function refrescarTodo() {
    cargarMetricas();
    cargarProductos();
    cargarPedidos();
    cargarUsuarios();
    cargarCategoriasAdmin();
}

document.addEventListener('DOMContentLoaded', () => {
    if (!document.getElementById('cuerpo-productos')) return;

    // Doble verificacion por si auth.js aun no ha redirigido.
    if (!Auth.estaAutenticado() || !Auth.esAdmin()) {
        return;
    }

    refrescarTodo();

    // ----- Productos -----
    document.getElementById('btn-nuevo-producto').addEventListener('click', () => abrirModalProducto(null));
    document.getElementById('form-producto').addEventListener('submit', guardarProducto);
    document.getElementById('form-stock').addEventListener('submit', guardarStock);

    document.getElementById('cuerpo-productos').addEventListener('click', evento => {
        const boton = evento.target.closest('button');
        if (!boton) return;
        const id = Number(boton.dataset.id);
        if (boton.classList.contains('js-editar')) {
            const producto = productosAdmin.find(p => Number(p.idProducto) === id);
            if (producto) abrirModalProducto(producto);
        } else if (boton.classList.contains('js-stock')) {
            const producto = productosAdmin.find(p => Number(p.idProducto) === id);
            if (producto) abrirModalStock(producto);
        } else if (boton.classList.contains('js-desactivar')) {
            desactivarProducto(id);
        }
    });

    // ----- Pedidos -----
    document.getElementById('cuerpo-pedidos').addEventListener('change', evento => {
        const selector = evento.target.closest('.js-estado-pedido');
        if (!selector) return;
        cambiarEstadoPedido(Number(selector.dataset.id), selector.value, selector);
    });
    document.getElementById('btn-refrescar-pedidos').addEventListener('click', cargarPedidos);

    // ----- Pagos -----
    document.getElementById('form-buscar-pagos').addEventListener('submit', evento => {
        evento.preventDefault();
        buscarPagos(document.getElementById('pago-id-pedido').value);
    });

    // ----- Usuarios -----
    document.getElementById('btn-refrescar-usuarios').addEventListener('click', cargarUsuarios);

    // ----- Actualizar todo -----
    document.getElementById('btn-refrescar-todo').addEventListener('click', refrescarTodo);
});
