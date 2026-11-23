# =====================================================================
#  FRONTEND - Tienda Virtual
#  Servidor nginx que publica los archivos estaticos y actua como
#  proxy reverso de /api/ hacia el API Gateway.
# =====================================================================
FROM nginx:1.27-alpine

# Configuracion del servidor (proxy reverso hacia el gateway)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Archivos estaticos
COPY index.html productos.html carrito.html login.html registro.html admin.html /usr/share/nginx/html/
COPY css /usr/share/nginx/html/css
COPY js /usr/share/nginx/html/js
COPY assets /usr/share/nginx/html/assets

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
