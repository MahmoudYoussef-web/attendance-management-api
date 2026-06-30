(function () {
  'use strict';

  function parseJwt(token) {
    try {
      var payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (_) {
      return null;
    }
  }

  function populateAvatars() {
    try {
      var token = localStorage.getItem('access_token');
      if (!token) return;
      var decoded = JSON.parse(atob(token.split('.')[1]));
      var email = decoded.sub || 'User';
      var initials = email.substring(0, 2).toUpperCase();
      var title = email + ' (' + (decoded.role || '') + ')';
      document.querySelectorAll('.user-avatar').forEach(function (el) {
        el.textContent = initials;
        el.title = title;
      });
    } catch (_) {}
  }

  window.AuthAPI = {
    login: async function (email, password) {
      var data = await window.API.post('/api/auth/login', { email: email, password: password });
      if (data && data.accessToken) {
        localStorage.setItem('access_token', data.accessToken);
        if (data.refreshToken) localStorage.setItem('refresh_token', data.refreshToken);
        var decoded = parseJwt(data.accessToken);
        return { success: true, role: decoded ? decoded.role : null };
      }
      return { success: false };
    },

    logout: async function () {
      try {
        await window.API.post('/api/auth/logout', {});
      } catch (_) {}
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      window.location.href = 'login.html';
    },

    getRole: function () {
      var token = localStorage.getItem('access_token');
      if (!token) return null;
      var decoded = parseJwt(token);
      return decoded ? decoded.role : null;
    },

    getToken: function () {
      return localStorage.getItem('access_token');
    },

    isLoggedIn: function () {
      var token = localStorage.getItem('access_token');
      if (!token) return false;
      var decoded = parseJwt(token);
      return decoded && decoded.exp * 1000 > Date.now();
    },

    parseJwt: parseJwt
  };

  populateAvatars();
})();
