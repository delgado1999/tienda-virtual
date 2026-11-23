/* =========================================================================
 * productos.js
 * Catalogo completo: buscador por nombre, filtro por categoria y grilla
 * responsive. Permite agregar productos al carrito.
 * ========================================================================= */

let productosCatalogo = [];

async function cargarCategoriasFiltro() {
    const selector = document.getElementById('filtro-categoria');
    if (!selector) return;
    try {
        const categorias = await API.get('/productos/categorias');
        if (Array.isArray(categorias)) {
            categorias.forEach(categoria => {
                const opcion = document.createElement('option');
                opcion.value = categoria;
                opcion.textContent = categoria;
                selector.appendChild(opcion);
            });
        }
    } catch (error) {
        Util.notificar('No se pudieron cargar las categorías.', 'warning');
    }

    // Preselecciona la categoria indicada en la URL (por ejemplo desde index.html).
    const parametros = new URLSearchParams(window.location.search);
    const categoriaUrl = parametros.get('categoria');
    if (categoriaUrl) {
        selector.value = categoriaUrl;
    }
    const nombreUrl = parametros.get('nombre');
    if (nombreUrl) {
        const entrada = document.getElementById('buscador-productos');
        if (entrada) entrada.value = nombreUrl;
    }
}

async function cargarProductos() {
    const grid = document.getElementById('grid-productos');
    const vacio = document.getElementById('productos-vacio');
    const conteo = document.getElementById('resultado-conteo');
    if (!grid) return;

    const nombre = (document.getElementById('buscador-productos') || {}).value || '';
    const categoria = (document.getElementById('filtro-categoria') || {}).value || '';

    const parametros = new URLSearchParams();
    if (nombre.trim()) parametros.append('nombre', nombre.trim());
    if (categoria) parametros.append('categoria', categoria);
    const consulta = parametros.toString();

    Util.mostrarCargando(grid, 'Cargando productos...');
    if (vacio) vacio.classList.add('d-none');

    try {
        const productos = await API.get('/productos' + (consulta ? '?' + consulta : ''));
        const lista = Array.isArray(productos) ? productos : [];
        productosCatalogo = lista;

        if (lista.length === 0) {
            grid.innerHTML = '';
            if (vacio) {
                vacio.classList.remove('d-none');
                vacio.innerHTML = Util.estadoVacio('bi-search', 'No se encontraron productos con los filtros seleccionados.');
            }
        } else {
            grid.innerHTML = lista.map(Util.tarjetaProducto).join('');
        }

        if (conteo) {
            conteo.textContent = lista.length + ' producto(s) encontrado(s)';
        }
    } catch (error) {
        grid.innerHTML = '';
        if (vacio) {
            vacio.classList.remove('d-none');
            vacio.innerHTML = Util.estadoVacio('bi-wifi-off', 'No se pudo cargar el catálogo.');
        }
        Util.notificar(error.message, 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const grid = document.getElementById('grid-productos');
    if (!grid) return;

    cargarCategoriasFiltro().then(cargarProductos);

    const formulario = document.getElementById('form-filtros');
    if (formulario) {
        formulario.addEventListener('submit', evento => {
            evento.preventDefault();
            cargarProductos();
        });
    }

    const limpiar = document.getElementById('btn-limpiar-filtros');
    if (limpiar) {
        limpiar.addEventListener('click', () => {
            const entrada = document.getElementById('buscador-productos');
            const selector = document.getElementById('filtro-categoria');
            if (entrada) entrada.value = '';
            if (selector) selector.value = '';
            cargarProductos();
        });
    }

    const buscador = document.getElementById('buscador-productos');
    if (buscador) {
        buscador.addEventListener('search', () => {
            if (buscador.value === '') cargarProductos();
        });
    }

    grid.addEventListener('click', evento => {
        const boton = evento.target.closest('.js-agregar-carrito');
        if (!boton) return;
        const id = Number(boton.dataset.id);
        const producto = productosCatalogo.find(p => Number(p.idProducto) === id);
        if (producto) Carrito.agregar(producto);
    });
});
