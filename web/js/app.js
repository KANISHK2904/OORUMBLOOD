/* ============================================================
   Oorum Blood – Blood Bank Management System
   app.js  –  Complete SPA logic
   ============================================================ */

'use strict';

/* ── API Base ─────────────────────────────────────────── */
const API = '/api';

/* ── State ────────────────────────────────────────────── */
let allRequests  = [];   // cached for filter/search
let allDonors    = [];
let currentSection = 'dashboard';

/* ── Blood groups palette for donut ─────────────────── */
const BG_COLORS = {
  'A+':'#b71c1c','A-':'#e53935','B+':'#1565c0','B-':'#42a5f5',
  'AB+':'#4a148c','AB-':'#7b1fa2','O+':'#e65100','O-':'#f57c00'
};

/* ── Utility: fetch JSON ──────────────────────────────── */
async function api(path, method = 'GET', body = null) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(API + path, opts);
  const json = await res.json();
  return { ok: res.ok, status: res.status, data: json };
}

/* ── Toast ─────────────────────────────────────────────── */
let toastTimer = null;
function toast(msg, type = 'success') {
  const el = document.getElementById('toast');
  el.textContent = msg;
  el.className = 'toast show ' + type;
  if (toastTimer) clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { el.className = 'toast'; }, 3500);
}

/* ── Confirm Dialog ────────────────────────────────────── */
function confirm(title, msg, onOk) {
  document.getElementById('confirmTitle').textContent = title;
  document.getElementById('confirmMessage').textContent = msg;
  document.getElementById('confirmModal').classList.remove('hidden');
  const okBtn = document.getElementById('confirmOk');
  const cancelBtn = document.getElementById('confirmCancel');
  const close = () => document.getElementById('confirmModal').classList.add('hidden');
  okBtn.onclick = () => { close(); onOk(); };
  cancelBtn.onclick = close;
}

/* ── Modal helpers ─────────────────────────────────────── */
function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

// Close modal on backdrop click
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', e => {
    if (e.target === overlay) overlay.classList.add('hidden');
  });
});

/* ── Navigation ────────────────────────────────────────── */
const SECTION_TITLES = {
  dashboard: 'Dashboard',
  donors:    'Donor Management',
  patients:  'Patient Management',
  hospitals: 'Hospital Management',
  inventory: 'Blood Inventory',
  donations: 'Blood Donations',
  requests:  'Blood Requests',
};

function navigate(section) {
  currentSection = section;
  // Update nav items
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.dataset.section === section);
  });
  // Update sections
  document.querySelectorAll('.page-section').forEach(el => {
    el.classList.toggle('active', el.id === 'section-' + section);
  });
  // Update header title
  document.getElementById('headerTitle').textContent = SECTION_TITLES[section] || section;
  // Load section data
  loadSection(section);
}

function loadSection(section) {
  switch (section) {
    case 'dashboard': loadDashboard(); break;
    case 'donors':    loadDonors();    break;
    case 'patients':  loadPatients();  break;
    case 'hospitals': loadHospitals(); break;
    case 'inventory': loadInventory(); break;
    case 'donations': loadDonations(); break;
    case 'requests':  loadRequests();  break;
  }
}

/* ── Clock ─────────────────────────────────────────────── */
function startClock() {
  const el = document.getElementById('headerClock');
  const tick = () => {
    const now = new Date();
    el.textContent = now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  };
  tick();
  setInterval(tick, 1000);
}

/* ================================================================
   DASHBOARD
   ================================================================ */
async function loadDashboard() {
  const { ok, data } = await api('/dashboard');
  if (!ok) return;
  const d = data.data;

  // KPI
  setText('kpi-donors',   d.totalDonors   ?? 0);
  setText('kpi-patients', d.totalPatients ?? 0);
  setText('kpi-units',    d.totalUnits    ?? 0);
  setText('kpi-pending',  d.pendingRequests ?? 0);
  setText('pendingBadge', (d.pendingRequests ?? 0) + ' Pending');

  // Bar chart
  renderBarChart('dashboardBarChart', d.bloodStock || []);

  // Donut
  renderDonut(d.bloodStock || []);

  // Recent requests table
  renderDashReqTable(d.recentRequests || []);

  // Recent donations feed
  renderDashDonationFeed(d.recentDonations || []);
}

function renderBarChart(containerId, stock) {
  const container = document.getElementById(containerId);
  if (!container) return;
  const max = Math.max(...stock.map(s => s.quantity), 100);
  container.innerHTML = stock.map(s => {
    const pct = max > 0 ? Math.max((s.quantity / max) * 100, s.quantity > 0 ? 2 : 0) : 0;
    const cls = stockClass(s.quantity);
    return `<div class="bar-col">
      <span class="bar-value ${cls === 'critical' ? 'critical' : ''}">${s.quantity}</span>
      <div class="bar-fill ${cls}" style="height:${pct.toFixed(1)}%"></div>
      <span class="bar-label ${cls === 'critical' ? 'critical' : ''}">${s.bloodGroup}</span>
      <span class="bar-status ${cls}">${cls}</span>
    </div>`;
  }).join('');
}

function renderDonut(stock) {
  const total = stock.reduce((s, x) => s + x.quantity, 0);
  setText('donutTotal', total);
  setText('donutSubtitle', `Aggregate ${total} units`);

  const svg = document.getElementById('donutSvg');
  const legend = document.getElementById('donutLegend');
  const r = 48, cx = 60, cy = 60;
  const circ = 2 * Math.PI * r;

  // Remove old segments (keep the grey base circle at index 0)
  while (svg.children.length > 1) svg.removeChild(svg.lastChild);

  let offset = 0;
  const groups = stock.filter(s => s.quantity > 0);

  legend.innerHTML = stock.map(s => `
    <div class="donut-legend-item">
      <span class="donut-dot" style="background:${BG_COLORS[s.bloodGroup] || '#aaa'}"></span>
      <span>${s.bloodGroup}: <b>${s.quantity}</b></span>
    </div>`).join('');

  if (total === 0) return;

  groups.forEach(s => {
    const pct   = s.quantity / total;
    const dash  = (pct * circ).toFixed(2);
    const gap   = (circ - pct * circ).toFixed(2);
    const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
    circle.setAttribute('cx', cx);
    circle.setAttribute('cy', cy);
    circle.setAttribute('r', r);
    circle.setAttribute('fill', 'none');
    circle.setAttribute('stroke', BG_COLORS[s.bloodGroup] || '#aaa');
    circle.setAttribute('stroke-width', '14');
    circle.setAttribute('stroke-dasharray', `${dash} ${gap}`);
    circle.setAttribute('stroke-dashoffset', (-offset * circ).toFixed(2));
    svg.appendChild(circle);
    offset += pct;
  });
}

function renderDashReqTable(requests) {
  const tbody = document.getElementById('dashReqBody');
  if (!requests.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">assignment_late</span>
      <p>No recent blood requests</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = requests.slice(0, 5).map(r => `
    <tr>
      <td class="cell-mono">#${r.id}</td>
      <td class="cell-name">${esc(r.patientName)}</td>
      <td>${esc(r.hospitalName)}</td>
      <td><span class="blood-group-pill">${esc(r.bloodGroup)}</span></td>
      <td class="text-center font-bold">${r.units}</td>
      <td>${statusBadge(r.status)}</td>
      <td class="text-right">
        ${r.status === 'Pending' ? `
          <button class="btn-fulfill" onclick="fulfillRequest(${r.id})">
            <span class="material-symbols-outlined">check_circle</span>Fulfill
          </button>` : '—'}
      </td>
    </tr>`).join('');
}

function renderDashDonationFeed(donations) {
  const feed = document.getElementById('dashDonationFeed');
  if (!donations.length) {
    feed.innerHTML = `<div class="empty-state">
      <span class="material-symbols-outlined empty-icon">volunteer_activism</span>
      <p>No donations recorded yet</p></div>`;
    return;
  }
  feed.innerHTML = donations.slice(0, 5).map(d => `
    <div class="donation-item">
      <div class="donation-item-left">
        <div class="donation-avatar">${esc(d.bloodGroup)}</div>
        <div>
          <div class="donation-name">${esc(d.donorName)}</div>
          <div class="donation-sub">${d.units} unit(s) · ${esc(d.bloodGroup)}</div>
        </div>
      </div>
      <div class="donation-item-right">
        <div class="donation-status">
          <span class="material-symbols-outlined">task_alt</span>Verified
        </div>
        <div class="donation-date">${fmtDate(d.donationDate)}</div>
      </div>
    </div>`).join('');
}

/* ================================================================
   DONORS
   ================================================================ */
async function loadDonors(query = '') {
  const path = query ? `/donors?search=${encodeURIComponent(query)}` : '/donors';
  const { ok, data } = await api(path);
  if (!ok) { toast('Failed to load donors', 'error'); return; }
  allDonors = data.data || [];
  renderDonorsTable(allDonors);
}

function renderDonorsTable(donors) {
  const tbody = document.getElementById('donorsTableBody');
  if (!donors.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">person_heart</span>
      <p class="empty-title">No donors registered yet</p>
      <p class="empty-sub">Click "Register Donor" to add your first donor.</p>
    </div></td></tr>`;
    return;
  }
  tbody.innerHTML = donors.map(d => `
    <tr>
      <td class="cell-mono">${d.id}</td>
      <td class="cell-name">${esc(d.name)}</td>
      <td>${d.age}</td>
      <td><span class="blood-group-pill">${esc(d.bloodGroup)}</span></td>
      <td>${esc(d.phone)}</td>
      <td>${esc(d.email || '—')}</td>
      <td>${d.lastDonationDate ? fmtDate(d.lastDonationDate) : '<span style="color:var(--on-surface-variant)">Never</span>'}</td>
      <td>
        <div class="action-group">
          <button class="btn-icon btn-icon-edit" title="Edit" onclick="editDonor(${d.id})">
            <span class="material-symbols-outlined">edit</span>
          </button>
          <button class="btn-icon btn-icon-delete" title="Delete" onclick="deleteDonor(${d.id},'${esc(d.name)}')">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

function openAddDonor() {
  document.getElementById('donorModalTitle').textContent = 'Register Donor';
  document.getElementById('donorSubmitBtn').textContent = 'Register Donor';
  document.getElementById('donorId').value = '';
  document.getElementById('donorForm').reset();
  hideFormError('donorFormError');
  openModal('donorModal');
}

async function editDonor(id) {
  const d = allDonors.find(x => x.id === id);
  if (!d) return;
  document.getElementById('donorModalTitle').textContent = 'Edit Donor';
  document.getElementById('donorSubmitBtn').textContent = 'Save Changes';
  document.getElementById('donorId').value = d.id;
  setVal('donorName', d.name);
  setVal('donorPhone', d.phone);
  setVal('donorEmail', d.email || '');
  setVal('donorAge', d.age);
  setVal('donorBloodGroup', d.bloodGroup);
  setVal('donorLastDate', d.lastDonationDate || '');
  setVal('donorAddress', d.address || '');
  hideFormError('donorFormError');
  openModal('donorModal');
}

async function submitDonor() {
  const id = document.getElementById('donorId').value;
  const body = {
    name:             getVal('donorName'),
    phone:            getVal('donorPhone'),
    email:            getVal('donorEmail'),
    age:              getVal('donorAge'),
    bloodGroup:       getVal('donorBloodGroup'),
    lastDonationDate: getVal('donorLastDate'),
    address:          getVal('donorAddress'),
  };
  const isEdit = !!id;
  const path = isEdit ? `/donors/${id}` : '/donors';
  const method = isEdit ? 'PUT' : 'POST';
  const { ok, data } = await api(path, method, body);
  if (!ok) { showFormError('donorFormError', data.error || 'An error occurred.'); return; }
  closeModal('donorModal');
  toast(isEdit ? 'Donor updated successfully.' : 'Donor registered successfully.');
  loadDonors();
}

function deleteDonor(id, name) {
  confirm('Delete Donor', `Remove "${name}" from the registry? All associated donations will also be deleted.`, async () => {
    const { ok, data } = await api(`/donors/${id}`, 'DELETE');
    if (!ok) { toast(data.error || 'Delete failed', 'error'); return; }
    toast('Donor deleted.');
    loadDonors();
  });
}

/* ================================================================
   PATIENTS
   ================================================================ */
let allPatients = [];

async function loadPatients() {
  const { ok, data } = await api('/patients');
  if (!ok) { toast('Failed to load patients', 'error'); return; }
  allPatients = data.data || [];
  renderPatientsTable(allPatients);
}

function renderPatientsTable(patients) {
  const tbody = document.getElementById('patientsTableBody');
  if (!patients.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">personal_injury</span>
      <p class="empty-title">No patients registered yet</p>
      <p class="empty-sub">Click "Add Patient" to add your first patient.</p>
    </div></td></tr>`;
    return;
  }
  tbody.innerHTML = patients.map(p => `
    <tr>
      <td class="cell-mono">${p.id}</td>
      <td class="cell-name">${esc(p.name)}</td>
      <td>${p.age}</td>
      <td><span class="blood-group-pill">${esc(p.bloodGroup)}</span></td>
      <td>${esc(p.phone)}</td>
      <td>${esc(p.disease || '—')}</td>
      <td>${p.hospitalName ? `<span style="font-weight:600">${esc(p.hospitalName)}</span>${p.hospitalCode ? `<br><span style="font-size:11px;color:var(--on-surface-variant)">${esc(p.hospitalCode)}</span>` : ''}` : '<span style="color:var(--on-surface-variant)">—</span>'}</td>
      <td>
        <div class="action-group">
          <button class="btn-icon btn-icon-edit" title="Edit" onclick="editPatient(${p.id})">
            <span class="material-symbols-outlined">edit</span>
          </button>
          <button class="btn-icon btn-icon-delete" title="Delete" onclick="deletePatient(${p.id},'${esc(p.name)}')">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

function openAddPatient() {
  document.getElementById('patientModalTitle').textContent = 'Add Patient';
  document.getElementById('patientSubmitBtn').textContent = 'Add Patient';
  document.getElementById('patientId').value = '';
  document.getElementById('patientForm').reset();
  resetPatientHospital();
  hideFormError('patientFormError');
  openModal('patientModal');
}

async function editPatient(id) {
  const p = allPatients.find(x => x.id === id);
  if (!p) return;
  document.getElementById('patientModalTitle').textContent = 'Edit Patient';
  document.getElementById('patientSubmitBtn').textContent = 'Save Changes';
  document.getElementById('patientId').value = p.id;
  setVal('patientName', p.name);
  setVal('patientPhone', p.phone);
  setVal('patientAge', p.age);
  setVal('patientBloodGroup', p.bloodGroup);
  setVal('patientDisease', p.disease || '');
  setVal('patientAddress', p.address || '');
  // Pre-fill hospital autocomplete
  if (p.hospitalId && p.hospitalId > 0 && p.hospitalName) {
    setVal('patientHospitalId', p.hospitalId);
    setVal('patientHospitalInput', p.hospitalCode
      ? `${p.hospitalName} (${p.hospitalCode})`
      : p.hospitalName);
  } else {
    resetPatientHospital();
  }
  hideFormError('patientFormError');
  openModal('patientModal');
}

async function submitPatient() {
  const id = document.getElementById('patientId').value;
  const body = {
    name:       getVal('patientName'),
    phone:      getVal('patientPhone'),
    age:        getVal('patientAge'),
    bloodGroup: getVal('patientBloodGroup'),
    disease:    getVal('patientDisease'),
    address:    getVal('patientAddress'),
    hospitalId: getVal('patientHospitalId') || '0',
  };
  const isEdit = !!id;
  const { ok, data } = await api(isEdit ? `/patients/${id}` : '/patients', isEdit ? 'PUT' : 'POST', body);
  if (!ok) { showFormError('patientFormError', data.error || 'An error occurred.'); return; }
  closeModal('patientModal');
  toast(isEdit ? 'Patient updated.' : 'Patient added.');
  loadPatients();
}

function deletePatient(id, name) {
  confirm('Delete Patient', `Remove patient "${name}"? All associated blood requests will also be deleted.`, async () => {
    const { ok, data } = await api(`/patients/${id}`, 'DELETE');
    if (!ok) { toast(data.error || 'Delete failed', 'error'); return; }
    toast('Patient deleted.');
    loadPatients();
  });
}

/* ================================================================
   HOSPITALS
   ================================================================ */
let allHospitals = [];

async function loadHospitals() {
  const { ok, data } = await api('/hospitals');
  if (!ok) { toast('Failed to load hospitals', 'error'); return; }
  allHospitals = data.data || [];
  renderHospitalsTable(allHospitals);
}

function renderHospitalsTable(hospitals) {
  const tbody = document.getElementById('hospitalsTableBody');
  if (!hospitals.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="7"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">local_hospital</span>
      <p class="empty-title">No hospitals registered yet</p>
      <p class="empty-sub">Click "Register Hospital" to add your first hospital.</p>
    </div></td></tr>`;
    return;
  }
  tbody.innerHTML = hospitals.map(h => `
    <tr>
      <td class="cell-mono">${h.id}</td>
      <td class="cell-name">${esc(h.name)}</td>
      <td>${esc(h.phone)}</td>
      <td>${esc(h.email || '—')}</td>
      <td class="cell-mono">${esc(h.registrationNumber || '—')}</td>
      <td>${esc(h.address || '—')}</td>
      <td>
        <div class="action-group">
          <button class="btn-icon btn-icon-edit" title="Edit" onclick="editHospital(${h.id})">
            <span class="material-symbols-outlined">edit</span>
          </button>
          <button class="btn-icon btn-icon-delete" title="Delete" onclick="deleteHospital(${h.id},'${esc(h.name)}')">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

function openAddHospital() {
  document.getElementById('hospitalModalTitle').textContent = 'Register Hospital';
  document.getElementById('hospitalSubmitBtn').textContent = 'Register Hospital';
  document.getElementById('hospitalId').value = '';
  document.getElementById('hospitalForm').reset();
  hideFormError('hospitalFormError');
  openModal('hospitalModal');
}

async function editHospital(id) {
  const h = allHospitals.find(x => x.id === id);
  if (!h) return;
  document.getElementById('hospitalModalTitle').textContent = 'Edit Hospital';
  document.getElementById('hospitalSubmitBtn').textContent = 'Save Changes';
  document.getElementById('hospitalId').value = h.id;
  setVal('hospitalName', h.name);
  setVal('hospitalPhone', h.phone);
  setVal('hospitalEmail', h.email || '');
  setVal('hospitalRegNo', h.registrationNumber || '');
  setVal('hospitalAddress', h.address || '');
  hideFormError('hospitalFormError');
  openModal('hospitalModal');
}

async function submitHospital() {
  const id = document.getElementById('hospitalId').value;
  const body = {
    name:               getVal('hospitalName'),
    phone:              getVal('hospitalPhone'),
    email:              getVal('hospitalEmail'),
    registrationNumber: getVal('hospitalRegNo'),
    address:            getVal('hospitalAddress'),
  };
  const isEdit = !!id;
  const { ok, data } = await api(isEdit ? `/hospitals/${id}` : '/hospitals', isEdit ? 'PUT' : 'POST', body);
  if (!ok) { showFormError('hospitalFormError', data.error || 'An error occurred.'); return; }
  closeModal('hospitalModal');
  toast(isEdit ? 'Hospital updated.' : 'Hospital registered.');
  loadHospitals();
}

function deleteHospital(id, name) {
  confirm('Delete Hospital', `Remove hospital "${name}"? All associated blood requests will also be deleted.`, async () => {
    const { ok, data } = await api(`/hospitals/${id}`, 'DELETE');
    if (!ok) { toast(data.error || 'Delete failed', 'error'); return; }
    toast('Hospital deleted.');
    loadHospitals();
  });
}

/* ================================================================
   BLOOD INVENTORY
   ================================================================ */
async function loadInventory() {
  const { ok, data } = await api('/stock');
  if (!ok) return;
  const stock = data.data || [];
  renderBloodGroupCards(stock);
  renderInventoryTable(stock);
}

function renderBloodGroupCards(stock) {
  const grid = document.getElementById('bloodGroupCards');
  const total = stock.reduce((s, x) => s + x.quantity, 0);
  grid.innerHTML = stock.map(s => {
    const cls     = stockClass(s.quantity);
    const pct     = total > 0 ? Math.min((s.quantity / total) * 100, 100).toFixed(1) : 0;
    const capPct  = Math.min((s.quantity / 100) * 100, 100).toFixed(1);
    const [letter, sign] = splitBG(s.bloodGroup);
    const statusLabel = cls === 'sufficient' ? 'Sufficient' : cls === 'low' ? 'Low Stock' : 'Critical';
    return `<div class="bg-card">
      <div class="bg-card-header">
        <div>
          <div class="bg-group-name">
            <span class="bg-group-letter">${letter}</span>
            <span class="bg-group-sign">${sign}</span>
          </div>
          <div class="bg-rh">${sign === '+' ? 'Rh Positive' : 'Rh Negative'}</div>
        </div>
        <span class="badge ${cls === 'sufficient' ? 'badge-success' : cls === 'low' ? 'badge-warning' : 'badge-error'}">${statusLabel}</span>
      </div>
      <div class="bg-body">
        <div>
          <div class="bg-units ${cls === 'critical' ? 'critical' : ''}">${s.quantity}</div>
          <div class="bg-units-label">available units</div>
        </div>
        <div class="bg-pct-right">
          <div class="bg-pct-val ${cls}">${pct}%</div>
          <div class="bg-pct-label ${cls}">of total</div>
        </div>
      </div>
      <div class="bg-progress-track">
        <div class="bg-progress-fill ${cls}" style="width:${capPct}%"></div>
      </div>
    </div>`;
  }).join('');
}

function renderInventoryTable(stock) {
  const tbody = document.getElementById('inventoryTableBody');
  tbody.innerHTML = stock.map(s => {
    const cls   = stockClass(s.quantity);
    const capPct = Math.min((s.quantity / 100) * 100, 100).toFixed(1);
    const label  = cls === 'sufficient' ? 'Sufficient' : cls === 'low' ? 'Low Stock' : 'Critical';
    return `<tr>
      <td><span class="blood-group-pill ${cls === 'critical' ? 'critical' : ''}">${esc(s.bloodGroup)}</span></td>
      <td>${s.bloodGroup.includes('+') ? 'Rh Positive (+)' : 'Rh Negative (−)'}</td>
      <td class="text-right font-bold font-mono">${s.quantity}</td>
      <td><span class="badge ${cls === 'sufficient' ? 'badge-success' : cls === 'low' ? 'badge-warning' : 'badge-error'}">${label}</span></td>
      <td>
        <div style="display:flex;align-items:center;gap:8px;">
          <div class="bg-progress-track" style="flex:1;max-width:120px;">
            <div class="bg-progress-fill ${cls}" style="width:${capPct}%"></div>
          </div>
          <span style="font-size:12px;color:var(--on-surface-variant)">${capPct}%</span>
        </div>
      </td>
    </tr>`;
  }).join('');
}

/* ================================================================
   DONATIONS
   ================================================================ */
async function loadDonations() {
  const { ok, data } = await api('/donations');
  if (!ok) { toast('Failed to load donations', 'error'); return; }
  renderDonationsTable(data.data || []);
}

function renderDonationsTable(donations) {
  const tbody = document.getElementById('donationsTableBody');
  if (!donations.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="5"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">volunteer_activism</span>
      <p class="empty-title">No donations recorded yet</p>
      <p class="empty-sub">Click "Record Donation" to log the first phlebotomy intake.</p>
    </div></td></tr>`;
    return;
  }
  tbody.innerHTML = donations.map(d => `
    <tr>
      <td class="cell-mono">${d.id}</td>
      <td class="cell-name">${esc(d.donorName)}</td>
      <td><span class="blood-group-pill">${esc(d.bloodGroup)}</span></td>
      <td class="text-center font-bold">${d.units}</td>
      <td>${fmtDate(d.donationDate)}</td>
    </tr>`).join('');
}

async function openAddDonation() {
  // Populate donor dropdown
  if (!allDonors.length) await loadDonors();
  const sel = document.getElementById('donationDonorId');
  sel.innerHTML = '<option value="">Select a donor</option>' +
    allDonors.map(d => `<option value="${d.id}">${esc(d.name)} (${esc(d.bloodGroup)})</option>`).join('');
  // Auto-fill blood group when donor selected
  sel.onchange = () => {
    const donor = allDonors.find(x => x.id == sel.value);
    if (donor) setVal('donationBloodGroup', donor.bloodGroup);
  };
  document.getElementById('donationForm').reset();
  // Reset dropdown again after reset
  sel.innerHTML = '<option value="">Select a donor</option>' +
    allDonors.map(d => `<option value="${d.id}">${esc(d.name)} (${esc(d.bloodGroup)})</option>`).join('');
  sel.onchange = () => {
    const donor = allDonors.find(x => x.id == sel.value);
    if (donor) setVal('donationBloodGroup', donor.bloodGroup);
  };
  // Default today
  setVal('donationDate', new Date().toISOString().split('T')[0]);
  hideFormError('donationFormError');
  openModal('donationModal');
}

async function submitDonation() {
  const body = {
    donorId:      getVal('donationDonorId'),
    bloodGroup:   getVal('donationBloodGroup'),
    units:        getVal('donationUnits'),
    donationDate: getVal('donationDate'),
  };
  const { ok, data } = await api('/donations', 'POST', body);
  if (!ok) { showFormError('donationFormError', data.error || 'An error occurred.'); return; }
  closeModal('donationModal');
  toast('Donation recorded and stock updated.');
  loadDonations();
  // Refresh inventory if visible
  if (currentSection === 'inventory') loadInventory();
}

/* ================================================================
   BLOOD REQUESTS
   ================================================================ */
let requestFilter = 'ALL';
let requestSearch = '';

async function loadRequests() {
  const { ok, data } = await api('/requests');
  if (!ok) { toast('Failed to load requests', 'error'); return; }
  allRequests = data.data || [];
  updateRequestStats(allRequests);
  applyRequestFilter();
}

function updateRequestStats(requests) {
  setText('reqTotal',     requests.length);
  setText('reqPending',   requests.filter(r => r.status === 'Pending').length);
  setText('reqFulfilled', requests.filter(r => r.status === 'Fulfilled').length);
  setText('reqRejected',  requests.filter(r => r.status === 'Rejected').length);
}

function applyRequestFilter() {
  let filtered = allRequests;
  if (requestFilter !== 'ALL') {
    filtered = filtered.filter(r => r.status === requestFilter);
  }
  if (requestSearch) {
    const q = requestSearch.toLowerCase();
    filtered = filtered.filter(r =>
      (r.patientName  || '').toLowerCase().includes(q) ||
      (r.hospitalName || '').toLowerCase().includes(q) ||
      (r.bloodGroup   || '').toLowerCase().includes(q)
    );
  }
  renderRequestsTable(filtered);
}

function renderRequestsTable(requests) {
  const tbody = document.getElementById('requestsTableBody');
  if (!requests.length) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8"><div class="empty-state">
      <span class="material-symbols-outlined empty-icon">assignment_late</span>
      <p class="empty-title">No blood requests found</p>
      <p class="empty-sub">Try adjusting the filter or create a new request.</p>
    </div></td></tr>`;
    return;
  }
  tbody.innerHTML = requests.map(r => `
    <tr>
      <td class="cell-mono">#${r.id}</td>
      <td class="cell-name">${esc(r.patientName)}</td>
      <td>${esc(r.hospitalName)}</td>
      <td><span class="blood-group-pill">${esc(r.bloodGroup)}</span></td>
      <td class="text-center font-bold">${r.units}</td>
      <td style="white-space:nowrap;font-size:12px;">${fmtDateTime(r.requestDate)}</td>
      <td>${statusBadge(r.status)}</td>
      <td>
        <div class="action-group">
          ${r.status === 'Pending' ? `
            <button class="btn-icon btn-icon-fulfill" title="Fulfill" onclick="fulfillRequest(${r.id})">
              <span class="material-symbols-outlined">check_circle</span>
            </button>
            <button class="btn-icon btn-icon-reject" title="Reject" onclick="rejectRequest(${r.id})">
              <span class="material-symbols-outlined">cancel</span>
            </button>
          ` : ''}
          <button class="btn-icon btn-icon-delete" title="Delete" onclick="deleteRequest(${r.id})">
            <span class="material-symbols-outlined">delete</span>
          </button>
        </div>
      </td>
    </tr>`).join('');
}

async function openAddRequest() {
  // Load patients and hospitals
  if (!allPatients.length)  await loadPatients();
  if (!allHospitals.length) await loadHospitals();

  const patSel = document.getElementById('reqPatientId');
  const hospSel = document.getElementById('reqHospitalId');
  patSel.innerHTML  = '<option value="">Select a patient</option>' +
    allPatients.map(p => `<option value="${p.id}">${esc(p.name)} (${esc(p.bloodGroup)})</option>`).join('');
  hospSel.innerHTML = '<option value="">Select a hospital</option>' +
    allHospitals.map(h => `<option value="${h.id}">${esc(h.name)}</option>`).join('');

  // Auto-fill blood group from patient
  patSel.onchange = () => {
    const pat = allPatients.find(x => x.id == patSel.value);
    if (pat) setVal('reqBloodGroup', pat.bloodGroup);
  };

  document.getElementById('requestForm').reset();
  patSel.innerHTML  = '<option value="">Select a patient</option>' +
    allPatients.map(p => `<option value="${p.id}">${esc(p.name)} (${esc(p.bloodGroup)})</option>`).join('');
  hospSel.innerHTML = '<option value="">Select a hospital</option>' +
    allHospitals.map(h => `<option value="${h.id}">${esc(h.name)}</option>`).join('');
  patSel.onchange = () => {
    const pat = allPatients.find(x => x.id == patSel.value);
    if (pat) setVal('reqBloodGroup', pat.bloodGroup);
  };

  hideFormError('requestFormError');
  openModal('requestModal');
}

async function submitRequest() {
  const body = {
    patientId:  getVal('reqPatientId'),
    hospitalId: getVal('reqHospitalId'),
    bloodGroup: getVal('reqBloodGroup'),
    units:      getVal('reqUnits'),
  };
  const { ok, data } = await api('/requests', 'POST', body);
  if (!ok) { showFormError('requestFormError', data.error || 'An error occurred.'); return; }
  closeModal('requestModal');
  toast('Blood request created.');
  loadRequests();
}

async function fulfillRequest(id) {
  const { ok, data } = await api(`/requests/${id}/fulfill`, 'PUT');
  if (!ok) {
    toast(data.error || 'Fulfillment failed – insufficient stock.', 'error');
  } else {
    toast('Request fulfilled. Stock updated.');
    loadRequests();
    if (currentSection === 'dashboard') loadDashboard();
    if (currentSection === 'inventory') loadInventory();
  }
}

async function rejectRequest(id) {
  confirm('Reject Request', 'Mark this blood request as Rejected?', async () => {
    const { ok, data } = await api(`/requests/${id}/reject`, 'PUT');
    if (!ok) { toast(data.error || 'Reject failed', 'error'); return; }
    toast('Request rejected.');
    loadRequests();
  });
}

function deleteRequest(id) {
  confirm('Delete Request', `Permanently delete blood request #${id}?`, async () => {
    const { ok, data } = await api(`/requests/${id}`, 'DELETE');
    if (!ok) { toast(data.error || 'Delete failed', 'error'); return; }
    toast('Request deleted.');
    loadRequests();
  });
}

/* ================================================================
   HELPERS
   ================================================================ */
function esc(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

function getVal(id) {
  const el = document.getElementById(id);
  return el ? el.value.trim() : '';
}

function setVal(id, val) {
  const el = document.getElementById(id);
  if (el) el.value = val;
}

function showFormError(id, msg) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = '⚠ ' + msg;
  el.classList.remove('hidden');
}

function hideFormError(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('hidden');
}

function statusBadge(status) {
  const map = {
    'Pending':   'status-pending',
    'Fulfilled': 'status-fulfilled',
    'Rejected':  'status-rejected',
  };
  return `<span class="badge ${map[status] || 'badge-neutral'}">${esc(status)}</span>`;
}

function stockClass(qty) {
  if (qty < 15) return 'critical';
  if (qty < 30) return 'low';
  return 'sufficient';
}

function splitBG(bg) {
  const m = bg.match(/^([A-Z]+)([+-])$/);
  return m ? [m[1], m[2]] : [bg, ''];
}

function fmtDate(str) {
  if (!str) return '—';
  try {
    const d = new Date(str.replace(' ', 'T'));
    if (isNaN(d)) return str;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  } catch { return str; }
}

function fmtDateTime(str) {
  if (!str) return '—';
  try {
    const d = new Date(str.replace(' ', 'T'));
    if (isNaN(d)) return str;
    return d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) +
      '<br><span style="color:var(--on-surface-variant)">' +
      d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' }) + '</span>';
  } catch { return str; }
}

/* ================================================================
   EVENT WIRING
   ================================================================ */
document.addEventListener('DOMContentLoaded', () => {
  startClock();

  // Sidebar navigation
  document.querySelectorAll('.nav-item[data-section]').forEach(el => {
    el.addEventListener('click', e => {
      e.preventDefault();
      navigate(el.dataset.section);
    });
  });

  // Donor section
  document.getElementById('btnAddDonor').addEventListener('click', openAddDonor);
  document.getElementById('donorSearch').addEventListener('input', e => {
    loadDonors(e.target.value.trim());
  });

  // Patient section
  document.getElementById('btnAddPatient').addEventListener('click', openAddPatient);

  // Hospital section
  document.getElementById('btnAddHospital').addEventListener('click', openAddHospital);

  // Donation section
  document.getElementById('btnAddDonation').addEventListener('click', openAddDonation);

  // Blood Request section
  document.getElementById('btnAddRequest').addEventListener('click', openAddRequest);

  // Request status filter tabs
  document.getElementById('requestStatusTabs').addEventListener('click', e => {
    const btn = e.target.closest('.tab-btn');
    if (!btn) return;
    document.querySelectorAll('#requestStatusTabs .tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    requestFilter = btn.dataset.status;
    applyRequestFilter();
  });

  // Request search
  document.getElementById('requestSearch').addEventListener('input', e => {
    requestSearch = e.target.value.trim();
    applyRequestFilter();
  });

  // Global search (navigates to correct section)
  document.getElementById('globalSearch').addEventListener('input', e => {
    const q = e.target.value.trim();
    if (!q) return;
    // Focus donor search
    if (currentSection === 'donors') {
      setVal('donorSearch', q);
      loadDonors(q);
    } else if (currentSection === 'requests') {
      requestSearch = q;
      setVal('requestSearch', q);
      applyRequestFilter();
    }
  });

  // Load initial section
  loadDashboard();
});

/* expose globals used by inline onclick handlers */
window.navigate       = navigate;
window.closeModal     = closeModal;
window.submitDonor    = submitDonor;
window.editDonor      = editDonor;
window.deleteDonor    = deleteDonor;
window.submitPatient  = submitPatient;
window.editPatient    = editPatient;
window.deletePatient  = deletePatient;
window.submitHospital = submitHospital;
window.editHospital   = editHospital;
window.deleteHospital = deleteHospital;
window.submitDonation = submitDonation;
window.openAddRequest = openAddRequest;
window.submitRequest  = submitRequest;
window.fulfillRequest = fulfillRequest;
window.rejectRequest  = rejectRequest;
window.deleteRequest  = deleteRequest;

/* ================================================================
   PATIENT HOSPITAL AUTOCOMPLETE
   ================================================================ */
let _hospSearchTimer   = null;
let _hospDropdownOpen  = false;
let _hospSearchResults = [];   // cached so we can look up by index safely

/** Called by oninput on #patientHospitalInput */
async function onPatientHospitalInput(val) {
  if (!val || !val.trim()) {
    setVal('patientHospitalId', '0');
    closePatientHospitalDropdown();
    return;
  }
  clearTimeout(_hospSearchTimer);
  _hospSearchTimer = setTimeout(() => _doHospSearch(val.trim()), 300);
}

async function _doHospSearch(q) {
  if (q.length < 1) return;
  const { ok, data } = await api(`/hospitals/search?q=${encodeURIComponent(q)}`);
  _hospSearchResults = ok ? (data.data || []) : [];
  renderPatientHospitalDropdown(_hospSearchResults);
}

function renderPatientHospitalDropdown(results) {
  const dd = document.getElementById('patientHospitalDropdown');
  dd.style.display = 'block';
  _hospDropdownOpen = true;

  if (!results.length) {
    dd.innerHTML = `
      <div style="padding:10px 12px;font-size:13px;color:var(--on-surface-variant);">No hospital found</div>
      <div style="padding:4px 12px 10px;font-size:12px;">
        <a href="#" onclick="navigate('hospitals');closePatientHospitalDropdown();return false;"
           style="color:var(--primary);font-weight:600;">
          Add hospital in Hospital Management →
        </a>
      </div>`;
    return;
  }

  // Use data-idx only — NO inline event handlers with interpolated strings
  dd.innerHTML = results.map((h, i) => `
    <div class="_hosp-option" data-idx="${i}"
         style="padding:8px 12px;cursor:pointer;border-bottom:1px solid var(--surface-container);">
      <div style="font-size:13px;font-weight:600;color:var(--on-surface);">${esc(h.name)}</div>
      ${h.code
        ? `<div style="font-size:11px;color:var(--on-surface-variant);">${esc(h.code)}${h.address ? ' · ' + esc(h.address) : ''}</div>`
        : (h.address ? `<div style="font-size:11px;color:var(--on-surface-variant);">${esc(h.address)}</div>` : '')}
    </div>`).join('');
}

function selectPatientHospital(id, name, code) {
  setVal('patientHospitalId', id);
  setVal('patientHospitalInput', code ? `${name} (${code})` : name);
  closePatientHospitalDropdown();
}

function closePatientHospitalDropdown() {
  const dd = document.getElementById('patientHospitalDropdown');
  if (dd) dd.style.display = 'none';
  _hospDropdownOpen = false;
}

function resetPatientHospital() {
  setVal('patientHospitalId', '0');
  setVal('patientHospitalInput', '');
  closePatientHospitalDropdown();
}

// ── Delegated click handler on the dropdown ──────────────────────────────────
// Must use mousedown (not click) so it fires before the input's blur event.
document.getElementById('patientHospitalDropdown').addEventListener('mousedown', e => {
  const opt = e.target.closest('._hosp-option');
  if (!opt) return;
  e.preventDefault(); // prevent input blur before we can read the value
  const idx = parseInt(opt.dataset.idx);
  const h = _hospSearchResults[idx];
  if (h) selectPatientHospital(h.hospitalId, h.name, h.code || '');
});

// Hover highlight for dropdown items (replaces removed inline onmouseover/out)
document.getElementById('patientHospitalDropdown').addEventListener('mouseover', e => {
  const opt = e.target.closest('._hosp-option');
  if (!opt) return;
  document.querySelectorAll('#patientHospitalDropdown ._hosp-option').forEach(o => {
    o.style.background = '';
    o.classList.remove('_focused');
  });
  opt.style.background = 'var(--surface-container-low)';
  opt.classList.add('_focused');
});
document.getElementById('patientHospitalDropdown').addEventListener('mouseout', e => {
  const opt = e.target.closest('._hosp-option');
  if (opt) { opt.style.background = ''; opt.classList.remove('_focused'); }
});

// Close dropdown when clicking outside
document.addEventListener('click', e => {
  const inp = document.getElementById('patientHospitalInput');
  const dd  = document.getElementById('patientHospitalDropdown');
  if (dd && inp && !inp.contains(e.target) && !dd.contains(e.target)) {
    closePatientHospitalDropdown();
  }
});

// Keyboard navigation inside the dropdown
document.addEventListener('keydown', e => {
  if (!_hospDropdownOpen) return;
  const dd = document.getElementById('patientHospitalDropdown');
  if (!dd) return;
  const opts = dd.querySelectorAll('._hosp-option');
  if (!opts.length) { if (e.key === 'Escape') closePatientHospitalDropdown(); return; }

  const focused = dd.querySelector('._hosp-option._focused');
  let idx = focused ? parseInt(focused.dataset.idx) : -1;

  if (e.key === 'ArrowDown') {
    e.preventDefault();
    idx = Math.min(idx + 1, opts.length - 1);
  } else if (e.key === 'ArrowUp') {
    e.preventDefault();
    idx = Math.max(idx - 1, 0);
  } else if (e.key === 'Enter' && idx >= 0) {
    e.preventDefault();
    opts[idx].dispatchEvent(new MouseEvent('mousedown'));
    return;
  } else if (e.key === 'Escape') {
    closePatientHospitalDropdown();
    return;
  } else { return; }

  opts.forEach(o => o.classList.remove('_focused'));
  opts[idx].classList.add('_focused');
  opts[idx].style.background = 'var(--surface-container-low)';
  opts.forEach((o, i) => { if (i !== idx) o.style.background = ''; });
});

// expose autocomplete input handler for inline oninput attribute
window.onPatientHospitalInput = onPatientHospitalInput;
window.closePatientHospitalDropdown = closePatientHospitalDropdown;
window.selectPatientHospital = selectPatientHospital;
