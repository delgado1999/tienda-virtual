/* =========================================================================
 * index.js
 * Pagina principal: categorias, productos destacados (4) y catalogo con
 * buscador por nombre.
 * ========================================================================= */

let productosIndex = [];

/** Mantiene un indice por id de todos los productos cargados en la pagina. */
function registrarProductos(lista) {
    lista.forEach(producto => {
        const existente = productosIndex.findIndex(p => Number(p.idProducto) === Number(producto.idProducto));
        if (existente >= 0) {
            productosIndex[existente] = producto;
        } else {
            productosIndex.push(producto);
        }
    });
}

async function cargarCategoriasIndex() {
    const contenedor = document.getElementById('lista-categorias');
    if (!contenedor) return;
    try {
        const categorias = await API.get('/productos/categorias');
        if (!Array.isArray(categorias) || categorias.length === 0) {
            contenedor.innerHTML = '<p class="text-muted mb-0">Aún no hay categorías registradas.</p>';
            return;
        }
        let html = '';
        categorias.forEach(categoria => {
            const destino = 'productos.html?categoria=' + encodeURIComponent(categoria);
            html += '<a class="chip-categoria" href="' + destino + '">' +
                        '<i class="bi bi-tag"></i>' + Util.escapar(categoria) +
                    '</a>';
        });
        contenedor.innerHTML = html;
    } catch (error) {
        contenedor.innerHTML = '<p class="text-muted mb-0">No se pudieron cargar las categorías.</p>';
        Util.notificar(error.message, 'danger');
    }
}

function agregarProductoDesdeBoton(boton) {
    const id = Number(boton.dataset.id);
    const producto = productosIndex.find(p => Number(p.idProducto) === id);
    if (!producto) return;
    Carrito.agregar(producto);
}

async function cargarDestacados() {
    const contenedor = document.getElementById('productos-destacados');
    if (!contenedor) return;
    try {
        const productos = await API.get('/productos');
        const lista = Array.isArray(productos) ? productos : [];
        registrarProductos(lista);
        const destacados = lista.slice(0, 4);

        if (destacados.length === 0) {
            contenedor.innerHTML = '<div class="col-12">' + Util.estadoVacio('bi-box-seam', 'Todavía no hay productos destacados.') + '</div>';
            return;
        }
        contenedor.innerHTML = destacados.map(Util.tarjetaProducto).join('');
    } catch (error) {
        contenedor.innerHTML = '<div class="col-12">' + Util.estadoVacio('bi-wifi-off', 'No se pudieron cargar los productos.') + '</div>';
        Util.notificar(error.message, 'danger');
    }
}

async function cargarCatalogo(nombre) {
    const contenedor = document.getElementById('catalogo-productos');
    const vacio = document.getElementById('catalogo-vacio');
    const conteo = document.getElementById('resultado-conteo');
    if (!contenedor) return;

    Util.mostrarCargando(contenedor, 'Cargando catálogo...');
    if (vacio) vacio.classList.add('d-none');

    try {
        const ruta = nombre ? '/productos?nombre=' + encodeURIComponent(nombre) : '/productos';
        const productos = await API.get(ruta);
        const lista = Array.isArray(productos) ? productos : [];
        registrarProductos(lista);

        if (lista.length === 0) {
            contenedor.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-search', nombre
                    ? 'No se encontraron productos para "' + nombre + '".'
                    : 'No hay productos disponibles por el momento.');
            }
        } else {
            contenedor.innerHTML = lista.map(Util.tarjetaProducto).join('');
        }

        if (conteo) {
            conteo.textContent = nombre
                ? lista.length + ' resultado(s) para "' + nombre + '"'
                : lista.length + ' producto(s) disponibles';
        }
    } catch (error) {
        contenedor.innerHTML = '';
        if (vacio) {
            vacio.classList.remove('d-none');
            vacio.innerHTML = Util.estadoVacio('bi-wifi-off', 'No se pudo cargar el catálogo.');
        }
        Util.notificar(error.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const catalogo = document.getElementById('catalogo-productos');
    if (!catalogo) return;

    cargarCategoriasIndex();
    cargarDestacados();
    cargarCatalogo('');

    const formulario = document.getElementById('form-busqueda-index');
    if (formulario) {
        formulario.addEventListener('submit', evento => {
            evento.preventDefault();
            const entrada = document.getElementById('buscador-index');
            cargarCatalogo(entrada ? entrada.value.trim() : '');
        });
    }

    const destacados = document.getElementById('productos-destacados');
    if (destacados) {
        destacados.addEventListener('click', evento => {
            const boton = evento.target.closest('.js-agregar-carrito');
            if (boton) agregarProductoDesdeBoton(boton);
        });
    }

    catalogo.addEventListener('click', evento => {
        const boton = evento.target.closest('.js-agregar-carrito');
        if (boton) agregarProductoDesdeBoton(boton);
    });
});
