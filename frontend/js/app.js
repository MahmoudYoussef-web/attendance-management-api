/* ============================================================
   Attendance Management System — Shared App Logic
   ============================================================ */

(function () {
  'use strict';

  // ==========================================================
  // Mock data
  // ==========================================================
  var MOCK = {
    employees: [
      { id: 1, name: 'Sarah Ahmed', email: 'sarah@company.com', code: 'EMP001', department: 'Engineering', schedule: 'Standard (9-5)', hireDate: '2023-03-15', status: 'ACTIVE' },
      { id: 2, name: 'Omar Hassan', email: 'omar@company.com', code: 'EMP002', department: 'Marketing', schedule: 'Standard (9-5)', hireDate: '2023-06-01', status: 'ACTIVE' },
      { id: 3, name: 'Lina Khalid', email: 'lina@company.com', code: 'EMP003', department: 'HR', schedule: 'Standard (9-5)', hireDate: '2023-01-10', status: 'ACTIVE' },
      { id: 4, name: 'Khalid Ali', email: 'khalid@company.com', code: 'EMP004', department: 'Engineering', schedule: 'Flex (10-6)', hireDate: '2024-02-20', status: 'ACTIVE' },
      { id: 5, name: 'Nora Saleem', email: 'nora@company.com', code: 'EMP005', department: 'Finance', schedule: 'Standard (9-5)', hireDate: '2022-11-05', status: 'ACTIVE' },
      { id: 6, name: 'Faisal Omar', email: 'faisal@company.com', code: 'EMP006', department: 'Engineering', schedule: 'Standard (9-5)', hireDate: '2024-05-12', status: 'INACTIVE' },
      { id: 7, name: 'Mona Adel', email: 'mona@company.com', code: 'EMP007', department: 'Marketing', schedule: 'Standard (9-5)', hireDate: '2023-09-01', status: 'ACTIVE' },
      { id: 8, name: 'Yusuf Ibrahim', email: 'yusuf@company.com', code: 'EMP008', department: 'Operations', schedule: 'Flex (10-6)', hireDate: '2024-01-15', status: 'ACTIVE' }
    ],
    departments: [
      { id: 1, name: 'Engineering', headCount: 3 },
      { id: 2, name: 'Marketing', headCount: 2 },
      { id: 3, name: 'HR', headCount: 1 },
      { id: 4, name: 'Finance', headCount: 1 },
      { id: 5, name: 'Operations', headCount: 1 }
    ],
    schedules: [
      { id: 1, name: 'Standard (9-5)', start: '09:00', end: '17:00', lateAfter: 15, halfDayAfter: 240 },
      { id: 2, name: 'Flex (10-6)', start: '10:00', end: '18:00', lateAfter: 15, halfDayAfter: 240 },
      { id: 3, name: 'Morning (7-3)', start: '07:00', end: '15:00', lateAfter: 10, halfDayAfter: 240 }
    ],
    attendance: [
      { id: 1, employee: 'Sarah Ahmed', date: '2026-06-28', checkIn: '08:55', status: 'PRESENT', notes: '' },
      { id: 2, employee: 'Omar Hassan', date: '2026-06-28', checkIn: '09:12', status: 'LATE', notes: 'Traffic' },
      { id: 3, employee: 'Lina Khalid', date: '2026-06-28', checkIn: '08:48', status: 'PRESENT', notes: '' },
      { id: 4, employee: 'Khalid Ali', date: '2026-06-28', checkIn: '10:05', status: 'LATE', notes: 'Overslept' },
      { id: 5, employee: 'Nora Saleem', date: '2026-06-28', checkIn: '08:58', status: 'PRESENT', notes: '' },
      { id: 6, employee: 'Faisal Omar', date: '2026-06-28', checkIn: '—', status: 'ABSENT', notes: 'No show' },
      { id: 7, employee: 'Mona Adel', date: '2026-06-28', checkIn: '—', status: 'ON_LEAVE', notes: 'Annual leave' },
      { id: 8, employee: 'Yusuf Ibrahim', date: '2026-06-28', checkIn: '10:22', status: 'LATE', notes: 'Appointment' },
      { id: 9, employee: 'Sarah Ahmed', date: '2026-06-27', checkIn: '08:50', status: 'PRESENT', notes: '' },
      { id: 10, employee: 'Omar Hassan', date: '2026-06-27', checkIn: '09:05', status: 'LATE', notes: '' },
      { id: 11, employee: 'Lina Khalid', date: '2026-06-27', checkIn: '08:45', status: 'PRESENT', notes: '' },
      { id: 12, employee: 'Nora Saleem', date: '2026-06-27', checkIn: '—', status: 'ON_LEAVE', notes: 'Annual leave' }
    ],
    leaves: [
      { id: 1, employee: 'Nora Saleem', type: 'ANNUAL', start: '2026-06-25', end: '2026-06-29', reason: 'Family vacation', status: 'APPROVED' },
      { id: 2, employee: 'Khalid Ali', type: 'SICK', start: '2026-07-01', end: '2026-07-02', reason: 'Flu', status: 'PENDING' },
      { id: 3, employee: 'Mona Adel', type: 'ANNUAL', start: '2026-06-28', end: '2026-06-28', reason: 'Personal day', status: 'APPROVED' },
      { id: 4, employee: 'Yusuf Ibrahim', type: 'EMERGENCY', start: '2026-06-30', end: '2026-06-30', reason: 'Family emergency', status: 'PENDING' }
    ],
    notifications: [
      { id: 1, text: 'Khalid Ali checked in late (10:05 AM)', time: '2 min ago', read: false },
      { id: 2, text: 'Nora Saleem submitted a sick leave request', time: '15 min ago', read: false },
      { id: 3, text: 'Sarah Ahmed requested department transfer', time: '1 hour ago', read: false },
      { id: 4, text: 'Monthly attendance report is ready', time: '3 hours ago', read: true },
      { id: 5, text: 'Omar Hassan updated his profile', time: '1 day ago', read: true }
    ],
    user: { name: 'Admin User', role: 'SUPER_ADMIN', initials: 'AU', email: 'admin@company.com' }
  };

  // ==========================================================
  // Helpers
  // ==========================================================
  function qs(sel, ctx) { return (ctx || document).querySelector(sel); }
  function qsa(sel, ctx) { return Array.prototype.slice.call((ctx || document).querySelectorAll(sel)); }

  function statusClass(status) {
    var map = {
      'PRESENT': 'present', 'ABSENT': 'absent', 'LATE': 'late', 'HALF_DAY': 'half-day', 'ON_LEAVE': 'on-leave',
      'ANNUAL': 'annual', 'SICK': 'sick', 'EMERGENCY': 'emergency', 'UNPAID': 'unpaid',
      'PENDING': 'pending', 'APPROVED': 'approved', 'REJECTED': 'rejected',
      'ACTIVE': 'active', 'INACTIVE': 'inactive', 'TERMINATED': 'terminated'
    };
    return map[status] || 'pending';
  }

  function statusLabel(status) {
    var map = {
      'PRESENT': 'Present', 'ABSENT': 'Absent', 'LATE': 'Late', 'HALF_DAY': 'Half Day', 'ON_LEAVE': 'On Leave',
      'ANNUAL': 'Annual', 'SICK': 'Sick', 'EMERGENCY': 'Emergency', 'UNPAID': 'Unpaid',
      'PENDING': 'Pending', 'APPROVED': 'Approved', 'REJECTED': 'Rejected',
      'ACTIVE': 'Active', 'INACTIVE': 'Inactive', 'TERMINATED': 'Terminated'
    };
    return map[status] || status;
  }

  function badgeHTML(type, status) {
    var cls = 'status-badge--' + statusClass(status);
    return '<span class="status-badge ' + cls + '">' + statusLabel(status) + '</span>';
  }

  function avatarHTML(name) {
    var parts = name.split(' ');
    var initials = parts.map(function (p) { return p[0]; }).join('').slice(0, 2).toUpperCase();
    return '<span class="emp-avatar">' + initials + '</span>';
  }

  function formatDate(dateStr) {
    var d = new Date(dateStr + 'T00:00:00');
    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return months[d.getMonth()] + ' ' + d.getDate() + ', ' + d.getFullYear();
  }

  // ==========================================================
  // Toast system
  // ==========================================================
  function showToast(message, type) {
    type = type || 'info';
    var container = qs('.toast-container');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    var toast = document.createElement('div');
    toast.className = 'toast toast--' + type;
    toast.innerHTML = '<span>' + message + '</span><button class="toast-close" onclick="this.parentElement.remove()">\u00d7</button>';
    container.appendChild(toast);
    setTimeout(function () { if (toast.parentElement) toast.remove(); }, 4000);
  }
  window.showToast = showToast;

  // ==========================================================
  // Sidebar
  // ==========================================================
  function initSidebar() {
    var toggle = qs('.sidebar-toggle');
    var sidebar = qs('.sidebar');
    if (!sidebar) return;

    if (toggle) {
      toggle.addEventListener('click', function () {
        sidebar.classList.toggle('collapsed');
      });
    }

    // Mobile
    var backdrop = document.createElement('div');
    backdrop.className = 'sidebar-backdrop';
    document.body.appendChild(backdrop);

    var mobileToggle = qs('.sidebar-toggle-mobile');
    if (mobileToggle) {
      mobileToggle.addEventListener('click', function () {
        sidebar.classList.add('mobile-open');
        backdrop.classList.add('open');
      });
    }

    backdrop.addEventListener('click', function () {
      sidebar.classList.remove('mobile-open');
      backdrop.classList.remove('open');
    });

    // Active nav item
    var navItems = qsa('.nav-item', sidebar);
    var path = location.pathname.split('/').pop() || 'index.html';
    navItems.forEach(function (item) {
      var href = item.getAttribute('href');
      if (href && href === path) {
        item.classList.add('active');
      }
    });
  }

  // ==========================================================
  // Modal
  // ==========================================================
  function openModal(id) {
    var modal = document.getElementById(id);
    if (modal) { modal.classList.add('open'); }
  }
  window.openModal = openModal;

  function closeModal(id) {
    var modal = document.getElementById(id);
    if (modal) { modal.classList.remove('open'); }
  }
  window.closeModal = closeModal;

  function initModals() {
    qsa('.modal-overlay').forEach(function (overlay) {
      var closeBtn = qs('.modal-close', overlay);
      if (closeBtn) {
        closeBtn.addEventListener('click', function () { overlay.classList.remove('open'); });
      }
      overlay.addEventListener('click', function (e) {
        if (e.target === overlay) overlay.classList.remove('open');
      });
    });
  }

  // ==========================================================
  // Notifications dropdown
  // ==========================================================
  function initNotifications() {
    var bell = qs('.bell-btn');
    var dropdown = qs('.notif-dropdown');
    if (!bell || !dropdown) return;

    bell.addEventListener('click', function (e) {
      e.stopPropagation();
      dropdown.classList.toggle('open');
    });

    document.addEventListener('click', function () {
      dropdown.classList.remove('open');
    });

    dropdown.addEventListener('click', function (e) { e.stopPropagation(); });
  }

  // ==========================================================
  // Tabs
  // ==========================================================
  function initTabs() {
    qsa('.tabs').forEach(function (tabBar) {
      var tabs = qsa('.tab', tabBar);
      tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
          tabs.forEach(function (t) { t.classList.remove('active'); });
          tab.classList.add('active');

          var target = tab.getAttribute('data-tab');
          if (target) {
            var panels = tabBar.parentElement.querySelectorAll('[data-panel]');
            panels.forEach(function (p) {
              p.style.display = p.getAttribute('data-panel') === target ? '' : 'none';
            });
          }
        });
      });
    });
  }

  // ==========================================================
  // Init on DOM ready
  // ==========================================================
  function init() {
    initSidebar();
    initModals();
    initNotifications();
    initTabs();
    // Mark notifications as shown
    var notifBadge = qs('.bell-badge');
    if (notifBadge) {
      var unread = MOCK.notifications.filter(function (n) { return !n.read; }).length;
      if (unread > 0) {
        notifBadge.textContent = unread;
      } else {
        notifBadge.style.display = 'none';
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.MOCK = MOCK;
  window.badgeHTML = badgeHTML;
  window.avatarHTML = avatarHTML;
  window.formatDate = formatDate;
  window.statusClass = statusClass;
  window.statusLabel = statusLabel;
  window.qs = qs;
  window.qsa = qsa;

})();
