const searchInput = document.getElementById('filtro');
const rows = Array.from(document.querySelectorAll('#filas tr'));
const stockFilterButtons = Array.from(document.querySelectorAll('[data-stock-filter]'));
const emptyMessage = document.getElementById('sinResultados');
let selectedStockFilter = 'all';

function applyFilters() {
  const searchText = searchInput.value.toLocaleLowerCase('es').trim();
  let visibleCount = 0;

  rows.forEach((row) => {
    const matchesSearch = row.innerText.toLocaleLowerCase('es').includes(searchText);
    const isLowStock = row.dataset.lowStock === 'true';
    const matchesStock = selectedStockFilter === 'all' || isLowStock;
    row.hidden = !(matchesSearch && matchesStock);
    if (!row.hidden) visibleCount += 1;
  });

  if (emptyMessage) emptyMessage.hidden = visibleCount > 0;
}

searchInput.addEventListener('input', applyFilters);

stockFilterButtons.forEach((button) => {
  button.addEventListener('click', () => {
    selectedStockFilter = button.dataset.stockFilter;

    stockFilterButtons.forEach((item) => {
      const isActive = item === button;
      item.classList.toggle('active', isActive);
      item.setAttribute('aria-pressed', String(isActive));
    });

    applyFilters();
  });
});