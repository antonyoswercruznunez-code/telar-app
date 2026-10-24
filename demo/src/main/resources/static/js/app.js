/* Comportamiento común a todas las pantallas: avisos que se desvanecen y tablas ordenables. */
(function () {
  'use strict';

  // 1) Los avisos de éxito se cierran solos a los 6 segundos.
  document.querySelectorAll('.notice.success').forEach(function (aviso) {
    setTimeout(function () {
      aviso.classList.add('se-va');
      setTimeout(function () { aviso.remove(); }, 600);
    }, 6000);
  });

  // 2) Al hacer clic en el encabezado de una columna se ordena la tabla (texto, números y fechas dd/mm/aaaa).
  function clave(celda) {
    var t = (celda ? celda.textContent : '').trim();
    var fecha = t.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
    return fecha ? fecha[3] + fecha[2] + fecha[1] + t.slice(10) : t;
  }
  function ordenar(tabla, indice, th) {
    var cuerpo = tabla.tBodies[0];
    if (!cuerpo) return;
    var direccion = th.dataset.dir === 'asc' ? 'desc' : 'asc';
    tabla.querySelectorAll('th.sortable').forEach(function (otro) { delete otro.dataset.dir; });
    th.dataset.dir = direccion;
    var filas = Array.prototype.slice.call(cuerpo.rows);
    filas.sort(function (a, b) {
      var r = clave(a.cells[indice]).localeCompare(clave(b.cells[indice]), 'es', { numeric: true, sensitivity: 'base' });
      return direccion === 'asc' ? r : -r;
    });
    filas.forEach(function (fila) { cuerpo.appendChild(fila); });
  }
  document.querySelectorAll('.table-scroll table').forEach(function (tabla) {
    if (!tabla.tHead || !tabla.tHead.rows.length) return;
    Array.prototype.forEach.call(tabla.tHead.rows[0].cells, function (th, i) {
      if (/acciones/i.test(th.textContent)) return;
      th.classList.add('sortable');
      th.title = 'Ordenar por esta columna';
      th.addEventListener('click', function () { ordenar(tabla, i, th); });
    });
  });
})();
