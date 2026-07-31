import { ApiClient } from '../shared/api/ApiClient.js';
import { SessionStore } from '../auth/infrastructure/SessionStore.js';

const emptyDashboard = {
  categories: [],
  movements: [],
  events: [],
  fixedExpenses: [],
};

export class AdultoFuncionalApp {
  constructor({
    root,
    apiClient = null,
    sessionStore = new SessionStore(),
  }) {
    if (!root) {
      throw new TypeError('AdultoFuncionalApp requiere un nodo root');
    }

    this.root = root;
    this.sessionStore = sessionStore;
    this.state = {
      account: this.sessionStore.getAccount(),
      apiBaseUrl: this.sessionStore.getApiBaseUrl(),
      authMode: 'login',
      dashboard: emptyDashboard,
      passwords: [],
      message: '',
      loading: false,
      authForm: {
        names: '',
        lastnames: '',
        phone: '',
        email: '',
        password: '',
        masterKey: '',
      },
      masterKeyForm: '',
    };
    this.apiClient = apiClient ?? new ApiClient({ baseUrl: this.state.apiBaseUrl });
  }

  mount() {
    this.render();
    if (this.state.account) {
      void this.loadDashboard({ silent: true });
    }
  }

  setState(partialState) {
    this.state = {
      ...this.state,
      ...partialState,
    };
    this.render();
  }

  render() {
    this.root.innerHTML = `
      <div class="app-shell">
        <header class="topbar">
          <div>
            <p class="eyebrow">Panel operativo</p>
            <h1>Adulto Funcional</h1>
            <p class="subtitle">Finanzas, agenda y credenciales seguras</p>
          </div>
          <div class="session-pill">${this.renderSessionLabel()}</div>
        </header>

        <main class="workspace">
          <section class="control-grid">
            ${this.renderApiPanel()}
            ${this.renderAuthPanel()}
          </section>

          ${this.renderMessage()}
          ${this.renderDashboard()}
          ${this.renderPasswordPanel()}
        </main>
      </div>
    `;

    this.bindEvents();
  }

  renderApiPanel() {
    return `
      <form class="panel compact-panel" data-form="api">
        <label class="field-label" for="apiBaseUrl">API</label>
        <div class="inline-form">
          <input id="apiBaseUrl" name="apiBaseUrl" value="${escapeHtml(this.state.apiBaseUrl)}" autocomplete="off" />
          <button class="secondary-button" type="submit">Guardar</button>
        </div>
      </form>
    `;
  }

  renderAuthPanel() {
    const isRegisterMode = this.state.authMode === 'register';
    return `
      <form class="panel auth-panel" data-form="auth">
        <div class="segmented" role="tablist" aria-label="Modo de autenticacion">
          <button class="${isRegisterMode ? 'segment-button' : 'segment-button active'}" type="button" data-auth-mode="login">Login</button>
          <button class="${isRegisterMode ? 'segment-button active' : 'segment-button'}" type="button" data-auth-mode="register">Registro</button>
        </div>

        ${isRegisterMode ? `
          <div class="form-grid">
            <label class="field">
              <span>Nombres</span>
              <input name="names" value="${escapeHtml(this.state.authForm.names)}" autocomplete="given-name" />
            </label>
            <label class="field">
              <span>Apellidos</span>
              <input name="lastnames" value="${escapeHtml(this.state.authForm.lastnames)}" autocomplete="family-name" />
            </label>
            <label class="field">
              <span>Telefono</span>
              <input name="phone" value="${escapeHtml(this.state.authForm.phone)}" autocomplete="tel" />
            </label>
            <label class="field">
              <span>Master Key</span>
              <input name="masterKey" value="${escapeHtml(this.state.authForm.masterKey)}" type="password" autocomplete="new-password" />
            </label>
          </div>
        ` : ''}

        <div class="form-grid">
          <label class="field">
            <span>Email</span>
            <input name="email" value="${escapeHtml(this.state.authForm.email)}" type="email" autocomplete="email" />
          </label>
          <label class="field">
            <span>Contrasena</span>
            <input name="password" value="${escapeHtml(this.state.authForm.password)}" type="password" autocomplete="${isRegisterMode ? 'new-password' : 'current-password'}" />
          </label>
        </div>

        <div class="actions-row">
          <button class="primary-button" type="submit" ${this.state.loading ? 'disabled' : ''}>${isRegisterMode ? 'Crear cuenta' : 'Entrar'}</button>
          ${this.state.account ? '<button class="ghost-button" type="button" data-action="logout">Salir</button>' : ''}
        </div>
      </form>
    `;
  }

  renderDashboard() {
    const { categories, movements, events, fixedExpenses } = this.state.dashboard;
    return `
      <section class="summary-grid" aria-label="Resumen">
        ${this.renderMetric('Categorias', categories.length)}
        ${this.renderMetric('Movimientos', movements.length)}
        ${this.renderMetric('Eventos', events.length)}
        ${this.renderMetric('Gastos fijos', fixedExpenses.length)}
      </section>

      <section class="content-grid">
        ${this.renderListPanel('Movimientos recientes', 'Sin movimientos', movements.slice(0, 6), (item) => ({
          title: item.description || item.movementType || 'Movimiento',
          meta: `${formatDate(item.movementDate)} · ${formatMoney(item.amount)}`,
          tag: item.category?.name ?? 'Sin categoria',
        }))}
        ${this.renderListPanel('Agenda', 'Sin eventos', events.slice(0, 6), (item) => ({
          title: item.title || 'Evento',
          meta: `${formatDate(item.eventDate)} · ${item.status ?? 'Sin estado'}`,
          tag: item.priority ?? 'Prioridad',
        }))}
        ${this.renderListPanel('Gastos fijos', 'Sin gastos fijos', fixedExpenses.slice(0, 6), (item) => ({
          title: item.name || 'Gasto fijo',
          meta: `${item.frequency ?? 'Frecuencia'} · ${formatMoney(item.amount)}`,
          tag: item.status ?? 'Estado',
        }))}
      </section>
    `;
  }

  renderPasswordPanel() {
    return `
      <section class="panel password-panel">
        <div class="panel-heading">
          <div>
            <h2>Gestor de claves</h2>
            <p>${this.state.passwords.length} credenciales visibles</p>
          </div>
          <button class="secondary-button" type="button" data-action="refresh-dashboard" ${this.state.loading ? 'disabled' : ''}>Actualizar</button>
        </div>

        <form class="inline-form" data-form="master-key">
          <input name="masterKey" value="${escapeHtml(this.state.masterKeyForm)}" type="password" autocomplete="current-password" placeholder="Master Key" />
          <button class="primary-button" type="submit" ${this.state.loading ? 'disabled' : ''}>Verificar</button>
        </form>

        <div class="table-list">
          ${this.state.passwords.length === 0 ? '<p class="empty-state">Sin credenciales visibles</p>' : this.state.passwords.map((item) => `
            <article class="list-item" data-id="${escapeHtml(item.id)}">
              <div>
                <strong>${escapeHtml(item.applicationName ?? 'Credencial')}</strong>
                <span>${escapeHtml(item.lastChangeDate ?? 'Sin fecha de cambio')}</span>
              </div>
              <span class="status-chip">Protegida</span>
            </article>
          `).join('')}
        </div>
      </section>
    `;
  }

  renderMetric(label, value) {
    return `
      <article class="metric">
        <strong>${value}</strong>
        <span>${label}</span>
      </article>
    `;
  }

  renderListPanel(title, empty, items, presenter) {
    return `
      <section class="panel list-panel">
        <div class="panel-heading">
          <h2>${escapeHtml(title)}</h2>
        </div>
        <div class="table-list">
          ${items.length === 0 ? `<p class="empty-state">${escapeHtml(empty)}</p>` : items.map((item) => {
            const row = presenter(item);
            return `
              <article class="list-item" data-id="${escapeHtml(item.id)}">
                <div>
                  <strong>${escapeHtml(row.title)}</strong>
                  <span>${escapeHtml(row.meta)}</span>
                </div>
                <span class="status-chip">${escapeHtml(row.tag)}</span>
              </article>
            `;
          }).join('')}
        </div>
      </section>
    `;
  }

  renderSessionLabel() {
    if (!this.state.account) {
      return 'Sin sesion';
    }

    return escapeHtml(`${this.state.account.names ?? ''} ${this.state.account.lastnames ?? ''}`.trim() || this.state.account.email);
  }

  renderMessage() {
    if (this.state.loading) {
      return '<p class="message loading">Procesando...</p>';
    }

    if (!this.state.message) {
      return '';
    }

    return `<p class="message">${escapeHtml(this.state.message)}</p>`;
  }

  bindEvents() {
    this.root.querySelector('[data-form="api"]')?.addEventListener('submit', (event) => {
      event.preventDefault();
      this.saveApiBaseUrl(new FormData(event.currentTarget).get('apiBaseUrl'));
    });

    this.root.querySelector('[data-form="auth"]')?.addEventListener('submit', (event) => {
      event.preventDefault();
      void this.submitAuth(event.currentTarget);
    });

    this.root.querySelectorAll('[data-auth-mode]').forEach((button) => {
      button.addEventListener('click', () => {
        this.setState({ authMode: button.dataset.authMode });
      });
    });

    this.root.querySelector('[data-action="logout"]')?.addEventListener('click', () => {
      void this.logout();
    });

    this.root.querySelector('[data-action="refresh-dashboard"]')?.addEventListener('click', () => {
      void this.loadDashboard();
    });

    this.root.querySelector('[data-form="master-key"]')?.addEventListener('submit', (event) => {
      event.preventDefault();
      void this.verifyMasterKey(event.currentTarget);
    });
  }

  saveApiBaseUrl(rawBaseUrl) {
    const apiBaseUrl = String(rawBaseUrl ?? '').trim();
    this.sessionStore.saveApiBaseUrl(apiBaseUrl);
    const storedUrl = this.sessionStore.getApiBaseUrl();
    this.apiClient.setBaseUrl(storedUrl);
    this.setState({
      apiBaseUrl: storedUrl,
      message: 'API actualizada',
    });
  }

  async submitAuth(form) {
    const authForm = Object.fromEntries(new FormData(form).entries());
    this.setState({
      authForm,
      loading: true,
      message: '',
    });

    try {
      const account = this.state.authMode === 'register'
        ? await this.apiClient.register({
          email: authForm.email,
          password: authForm.password,
          names: authForm.names,
          lastnames: authForm.lastnames,
          phone: authForm.phone,
          masterKey: authForm.masterKey,
        })
        : await this.apiClient.login({
          email: authForm.email,
          password: authForm.password,
        });

      this.sessionStore.saveAccount(account);
      this.state.account = account;
      await this.loadDashboard({ silent: true });
      this.setState({
        account,
        loading: false,
        message: this.state.authMode === 'register' ? 'Cuenta creada' : 'Sesion iniciada',
      });
    } catch (error) {
      this.setState({
        loading: false,
        message: normalizeError(error, 'No fue posible autenticar'),
      });
    }
  }

  async loadDashboard({ silent = false } = {}) {
    if (!this.state.account) {
      this.setState({ message: 'Inicia sesion para cargar datos' });
      return;
    }

    this.setState({
      loading: true,
      message: silent ? this.state.message : '',
    });

    try {
      const [categories, movements, events, fixedExpenses] = await Promise.all([
        this.apiClient.listCategories(),
        this.apiClient.listMovements(),
        this.apiClient.listEvents(),
        this.apiClient.listFixedExpenses(),
      ]);
      this.setState({
        dashboard: { categories, movements, events, fixedExpenses },
        loading: false,
        message: silent ? this.state.message : 'Datos actualizados',
      });
    } catch (error) {
      this.setState({
        loading: false,
        message: normalizeError(error, 'No fue posible cargar el panel'),
      });
    }
  }

  async logout() {
    this.setState({ loading: true, message: '' });

    try {
      await this.apiClient.logout();
    } catch {
      // La cookie se limpia del lado servidor cuando sea posible; el estado local se limpia siempre.
    }

    this.sessionStore.clearAccount();
    this.setState({
      account: null,
      dashboard: emptyDashboard,
      passwords: [],
      loading: false,
      message: 'Sesion cerrada',
    });
  }

  async verifyMasterKey(form) {
    if (!this.state.account) {
      this.setState({ message: 'Inicia sesion para acceder al gestor' });
      return;
    }

    const masterKey = String(new FormData(form).get('masterKey') ?? '');
    this.setState({
      masterKeyForm: masterKey,
      loading: true,
      message: '',
    });

    try {
      await this.apiClient.verifyMasterKey(masterKey);
      const passwords = await this.apiClient.listPasswords();
      this.setState({
        passwords,
        loading: false,
        message: 'Master Key verificada',
      });
    } catch (error) {
      this.setState({
        loading: false,
        message: normalizeError(error, 'No fue posible cargar credenciales'),
      });
    }
  }
}

function normalizeError(error, fallback) {
  return error instanceof Error ? error.message : fallback;
}

function formatDate(value) {
  if (!value) {
    return 'Sin fecha';
  }

  return String(value).slice(0, 10);
}

function formatMoney(value) {
  if (value === null || value === undefined || Number.isNaN(Number(value))) {
    return '$0';
  }

  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency: 'COP',
    maximumFractionDigits: 0,
  }).format(Number(value));
}

function escapeHtml(value) {
  return String(value ?? '')
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#039;');
}
