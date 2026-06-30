(function () {
  'use strict';

  var BASE_URL = 'http://localhost:8080';
  var MAX_RETRIES = 1;

  function getToken() {
    return localStorage.getItem('access_token');
  }

  function getRefreshToken() {
    return localStorage.getItem('refresh_token');
  }

  function setTokens(access, refresh) {
    localStorage.setItem('access_token', access);
    if (refresh) localStorage.setItem('refresh_token', refresh);
  }

  function clearTokens() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  function redirectLogin() {
    clearTokens();
    window.location.href = 'login.html';
  }

  async function tryRefresh() {
    var rt = getRefreshToken();
    if (!rt) return false;
    try {
      var res = await fetch(BASE_URL + '/api/auth/refresh', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken: rt })
      });
      if (!res.ok) return false;
      var data = await res.json();
      setTokens(data.accessToken, data.refreshToken);
      return true;
    } catch (_) {
      return false;
    }
  }

  async function request(method, path, body) {
    var headers = { 'Content-Type': 'application/json' };
    var token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    var opts = { method: method, headers: headers };
    if (body && method !== 'GET') opts.body = JSON.stringify(body);

    try {
      var res = await fetch(BASE_URL + path, opts);
      if (res.status === 401) {
        var refreshed = await tryRefresh();
        if (refreshed) {
          headers['Authorization'] = 'Bearer ' + getToken();
          opts.headers = headers;
          res = await fetch(BASE_URL + path, opts);
        } else {
          redirectLogin();
          return null;
        }
      }
      if (!res.ok) {
        var errBody;
        try { errBody = await res.json(); } catch (_) { errBody = null; }
        var msg = (errBody && errBody.message) || (errBody && errBody.error) || 'Request failed (' + res.status + ')';
        if (typeof window.showToast === 'function') {
          window.showToast(msg, 'error');
        }
        throw { status: res.status, message: msg, body: errBody };
      }
      if (res.status === 204) return null;
      return await res.json();
    } catch (e) {
      if (e.status) throw e;
      if (typeof window.showToast === 'function') {
        window.showToast('Network error — check your connection', 'error');
      }
      throw { status: 0, message: 'Network error', body: null };
    }
  }

  window.API = {
    get: function (path) { return request('GET', path); },
    post: function (path, body) { return request('POST', path, body); },
    put: function (path, body) { return request('PUT', path, body); },
    del: function (path) { return request('DELETE', path); }
  };
})();
