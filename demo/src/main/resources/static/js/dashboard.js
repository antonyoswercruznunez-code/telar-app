/* Dashboard: pide /dashboard/datos y pinta indicadores, gráficos y listas. Se refresca solo cada minuto. */
(function () {
  'use strict';
  var cuerpo = document.body;
  var urlDatos = cuerpo.dataset.datosUrl;
  var base = cuerpo.dataset.base || '/';
  var INTERVALO_MS = 60000;
  var temporizador = null;

  function $(selector) { return document.querySelector(selector); }
  function crear(etiqueta, clase, texto) {
    var e = document.createElement(etiqueta);
    if (clase) e.className = clase;
    if (texto !== undefined && texto !== null) e.textContent = texto;
    return e;
  }
  function vaciar(e) { while (e.firstChild) e.removeChild(e.firstChild); }
  function dinero(n) {
    return 'S/ ' + Number(n || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }
  function entero(n) { return Number(n || 0).toLocaleString('es-PE'); }
  function texto(clave, valor) {
    document.querySelectorAll('[data-kpi="' + clave + '"]').forEach(function (e) {
      e.textContent = valor;
      e.classList.remove('skeleton');
    });
  }
  function estado(mensaje, error) {
    var e = $('#dash-status');
    e.textContent = mensaje;
    e.classList.toggle('error', !!error);
  }

  function pintarIndicadores(r) {
    texto('ventasHoy', dinero(r.ventasHoy));
    texto('ventasHoyNota', entero(r.cantidadVentasHoy) + ' ventas hoy');
    texto('ventasMes', dinero(r.ventasMes));
    texto('ventasMesNota', entero(r.cantidadVentasMes) + ' ventas este mes');
    texto('saldoPorCobrar', dinero(r.saldoPorCobrar));
    texto('pedidosActivos', entero(r.pedidosActivos));
    texto('pedidosAtrasadosNota', r.pedidosAtrasados > 0 ? entero(r.pedidosAtrasados) + ' atrasados' : 'Ninguno atrasado');
    texto('alertasStock', entero(r.alertasStock));
    texto('clientesActivos', entero(r.clientesActivos));
    $('#kpi-stock').classList.toggle('kpi-alert', r.alertasStock > 0);
    $('#kpi-pedidos').classList.toggle('kpi-alert', r.pedidosAtrasados > 0);
  }

  function pintarBarras(puntos) {
    var contenedor = $('#grafico-ventas');
    vaciar(contenedor);
    var max = Math.max.apply(null, puntos.map(function (p) { return Number(p.valor); }).concat([1]));
    puntos.forEach(function (p, i) {
      var columna = crear('div', 'bar-col');
      columna.appendChild(crear('span', 'bar-value', Number(p.valor) > 0 ? dinero(p.valor) : ''));
      var barra = crear('div', 'bar' + (i === puntos.length - 1 ? ' bar-today' : ''));
      barra.title = p.etiqueta + ': ' + dinero(p.valor);
      barra.style.height = '0';
      columna.appendChild(barra);
      columna.appendChild(crear('span', 'bar-label', p.etiqueta));
      contenedor.appendChild(columna);
      var altura = (Number(p.valor) / max) * 78;
      requestAnimationFrame(function () { requestAnimationFrame(function () { barra.style.height = altura + '%'; }); });
    });
  }

  function pintarEstados(puntos) {
    var contenedor = $('#grafico-estados');
    vaciar(contenedor);
    var total = puntos.reduce(function (s, p) { return s + Number(p.valor); }, 0);
    if (total === 0) { contenedor.appendChild(crear('p', 'mini-empty', 'Aún no hay pedidos registrados.')); return; }
    var max = Math.max.apply(null, puntos.map(function (p) { return Number(p.valor); }));
    puntos.forEach(function (p) {
      var fila = crear('div', 'hbar-row');
      fila.appendChild(crear('span', '', p.etiqueta));
      var pista = crear('div', 'hbar-track');
      var relleno = crear('div', 'hbar-fill');
      pista.appendChild(relleno);
      fila.appendChild(pista);
      fila.appendChild(crear('span', 'hbar-count', entero(p.valor)));
      contenedor.appendChild(fila);
      requestAnimationFrame(function () { requestAnimationFrame(function () { relleno.style.width = (Number(p.valor) / max * 100) + '%'; }); });
    });
  }

  function lista(selector, filas, vacio, construir) {
    var ul = $(selector);
    vaciar(ul);
    if (!filas.length) { ul.appendChild(crear('li', 'mini-empty', vacio)); return; }
    filas.forEach(function (f) { ul.appendChild(construir(f)); });
  }
  function principal(titulo, subtitulo, href) {
    var cont = crear('div', 'mini-main');
    var t;
    if (href) { t = crear('a', 'text-link', titulo); t.href = href; var s = crear('strong'); s.appendChild(t); cont.appendChild(s); }
    else cont.appendChild(crear('strong', '', titulo));
    cont.appendChild(crear('span', '', subtitulo));
    return cont;
  }

  function pintarListas(r) {
    lista('#lista-pedidos', r.pedidosPorEntregar, 'No hay pedidos pendientes de entrega.', function (p) {
      var li = crear('li');
      li.appendChild(principal('#' + p.id + ' · ' + p.cliente, 'Entrega: ' + (p.entrega || 'sin fecha') + ' · ' + p.estado,
        base + 'pedidos/' + p.id + '/seguimiento'));
      if (Number(p.atrasado) === 1) li.appendChild(crear('span', 'pill pill-danger', 'Atrasado'));
      return li;
    });
    lista('#lista-stock', r.stockBajo, 'Todo el stock está por encima del mínimo.', function (s) {
      var li = crear('li');
      li.appendChild(principal(s.producto, s.color + ' · talla ' + s.talla));
      li.appendChild(crear('span', 'pill pill-danger', s.disponible + ' / mín. ' + s.minimo));
      return li;
    });
    lista('#lista-ventas', r.ultimasVentas, 'Aún no hay ventas registradas.', function (v) {
      var li = crear('li');
      li.appendChild(principal(v.cliente, v.fecha, base + 'gestion/venta/' + v.id + '/editar'));
      var derecha = crear('div', 'mini-main');
      derecha.style.textAlign = 'right';
      derecha.appendChild(crear('strong', '', dinero(v.total)));
      derecha.appendChild(crear('span', '', v.estadoPago));
      li.appendChild(derecha);
      return li;
    });
  }

  function cargar() {
    return fetch(urlDatos, { headers: { 'Accept': 'application/json' }, credentials: 'same-origin' })
      .then(function (resp) {
        if (!resp.ok) throw new Error('HTTP ' + resp.status);
        return resp.json();
      })
      .then(function (r) {
        pintarIndicadores(r);
        pintarBarras(r.ventasUltimosDias);
        pintarEstados(r.pedidosPorEstado);
        pintarListas(r);
        estado('Actualizado a las ' + new Date().toLocaleTimeString('es-PE', { hour: '2-digit', minute: '2-digit' }), false);
      })
      .catch(function () { estado('No se pudo actualizar. Reintentando…', true); });
  }

  function programar() {
    clearInterval(temporizador);
    temporizador = setInterval(function () { if (!document.hidden) cargar(); }, INTERVALO_MS);
  }

  $('#dash-actualizar').addEventListener('click', function () { estado('Actualizando…', false); cargar(); programar(); });
  document.addEventListener('visibilitychange', function () { if (!document.hidden) cargar(); });
  cargar();
  programar();
})();
