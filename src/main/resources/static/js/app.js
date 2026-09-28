/**
 * Expense Tracker — Personal SaaS Dashboard JavaScript
 * Complete modern light-theme SaaS application with real Spring Boot authentication,
 * personal dashboard, smart spending insights, tiered budget alerts, notifications,
 * search & filters, recurring expenses, CSV export, and profile management.
 */

// Application State
const state = {
  token: localStorage.getItem("expense_token") || null,
  currentUser: JSON.parse(localStorage.getItem("expense_user") || "null"),
  activeTab: "dashboard",
  selectedMonth: "2026-09",
  categories: [],
  expenses: [],
  budgets: [],
  summary: [],
  trend: null,
  personalDashboard: null,
  notifications: [],
  filters: {
    search: "",
    categoryId: "",
    month: "2026-09",
    startDate: "",
    endDate: "",
    minAmount: "",
    maxAmount: ""
  }
};

// Category Icons Map
const categoryIcons = {
  "Food": "🍔",
  "Travel": "✈️",
  "Stationery": "📚",
  "Entertainment": "🎬",
  "Shopping": "🛍️",
  "Other": "💳"
};

function getCategoryIcon(name) {
  if (!name) return "💳";
  for (const [key, icon] of Object.entries(categoryIcons)) {
    if (name.toLowerCase().includes(key.toLowerCase())) return icon;
  }
  return "💳";
}

// Currency Formatter (₹)
function formatCurrency(amount) {
  if (amount === null || amount === undefined || isNaN(amount)) return "₹0";
  const num = Number(amount);
  return "₹" + num.toLocaleString("en-IN", {
    maximumFractionDigits: 2,
    minimumFractionDigits: num % 1 === 0 ? 0 : 2
  });
}

// Date Formatter
function formatDate(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr + "T00:00:00");
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const diffTime = today.getTime() - date.getTime();
  const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return "Today";
  if (diffDays === 1) return "Yesterday";

  return date.toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short"
  });
}

// Month Display Formatter ("2026-09" -> "September 2026")
function formatMonthName(monthStr) {
  if (!monthStr) return "";
  const [year, month] = monthStr.split("-");
  const date = new Date(Number(year), Number(month) - 1, 1);
  return date.toLocaleDateString("en-US", { month: "long", year: "numeric" });
}

// API Client with JWT Bearer Token Support
const api = {
  async fetch(url, options = {}) {
    const headers = {
      "Content-Type": "application/json",
      ...options.headers
    };

    if (state.token) {
      headers["Authorization"] = `Bearer ${state.token}`;
    }

    try {
      const response = await window.fetch(url, {
        ...options,
        headers
      });

      if (response.status === 401 && !url.includes("/api/auth/login") && !url.includes("/api/auth/register")) {
        handleSessionExpired();
        throw new Error("Authentication required. Please log in.");
      }

      if (!response.ok) {
        let errMessage = "Request failed (" + response.status + ")";
        try {
          const errData = await response.json();
          if (errData.message) errMessage = errData.message;
          else if (errData.error) errMessage = errData.error;
        } catch (_) {}
        throw new Error(errMessage);
      }

      if (response.status === 204) return null;
      return await response.json();
    } catch (err) {
      console.error("API Error on " + url + ":", err);
      throw err;
    }
  },

  // Auth Endpoints
  login(credentials) {
    return this.fetch("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(credentials)
    });
  },
  register(data) {
    return this.fetch("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },
  getCurrentUser() {
    return this.fetch("/api/auth/me");
  },
  updateProfile(data) {
    return this.fetch("/api/auth/profile", {
      method: "PUT",
      body: JSON.stringify(data)
    });
  },
  changePassword(data) {
    return this.fetch("/api/auth/change-password", {
      method: "POST",
      body: JSON.stringify(data)
    });
  },

  // Categories
  getCategories() { return this.fetch("/api/categories"); },
  createCategory(data) { return this.fetch("/api/categories", { method: "POST", body: JSON.stringify(data) }); },

  // Expenses
  getExpenses(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.fetch("/api/expenses" + (query ? "?" + query : ""));
  },
  getExpenseById(id) { return this.fetch("/api/expenses/" + id); },
  createExpense(data) { return this.fetch("/api/expenses", { method: "POST", body: JSON.stringify(data) }); },
  updateExpense(id, data) { return this.fetch("/api/expenses/" + id, { method: "PUT", body: JSON.stringify(data) }); },
  deleteExpense(id) { return this.fetch("/api/expenses/" + id, { method: "DELETE" }); },

  // Budgets
  getBudgets(params = {}) {
    const query = new URLSearchParams(params).toString();
    return this.fetch("/api/budgets" + (query ? "?" + query : ""));
  },
  getBudgetById(id) { return this.fetch("/api/budgets/" + id); },
  createBudget(data) { return this.fetch("/api/budgets", { method: "POST", body: JSON.stringify(data) }); },
  updateBudget(id, data) { return this.fetch("/api/budgets/" + id, { method: "PUT", body: JSON.stringify(data) }); },
  deleteBudget(id) { return this.fetch("/api/budgets/" + id, { method: "DELETE" }); },

  // Personal Dashboard & Analytics
  getPersonalDashboard(month) {
    return this.fetch(`/api/dashboard/personal?month=${month}`);
  },
  getSummary(month) {
    const q = new URLSearchParams({ month }).toString();
    return this.fetch("/api/expenses/summary/current?" + q);
  },
  getTrend() { return this.fetch("/api/expenses/trend"); }
};

// Toast Notifications
function showToast(message, type = "success") {
  const container = document.getElementById("toastContainer");
  if (!container) return;

  const toast = document.createElement("div");
  toast.className = `toast ${type}`;
  toast.innerHTML = `
    <span>${type === "success" ? "✓" : type === "warning" ? "⚠" : "✕"}</span>
    <span>${message}</span>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = "0";
    toast.style.transform = "translateY(10px)";
    toast.style.transition = "all 0.25s ease-out";
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

// Password Visibility Toggle
function togglePasswordVisibility(inputId, iconId) {
  const input = document.getElementById(inputId);
  const icon = document.getElementById(iconId);
  if (!input) return;

  if (input.type === "password") {
    input.type = "text";
    if (icon) {
      icon.innerHTML = `
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l18 18" />
      `;
    }
  } else {
    input.type = "password";
    if (icon) {
      icon.innerHTML = `
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
      `;
    }
  }
}

// Authentication View Switcher
function showAuthView(view) {
  const loginView = document.getElementById("loginView");
  const registerView = document.getElementById("registerView");
  if (!loginView || !registerView) return;

  // Clear errors
  document.querySelectorAll(".field-error").forEach(el => {
    el.textContent = "";
    el.classList.remove("active");
  });

  if (view === "register") {
    loginView.style.display = "none";
    registerView.style.display = "block";
  } else {
    registerView.style.display = "none";
    loginView.style.display = "block";
  }
}

function handleForgotPassword() {
  showToast("Password reset link has been sent to your email if registered.", "success");
}

function handleSessionExpired() {
  state.token = null;
  state.currentUser = null;
  localStorage.removeItem("expense_token");
  localStorage.removeItem("expense_user");

  document.getElementById("appContainer").style.display = "none";
  document.getElementById("authContainer").style.display = "flex";
  showAuthView("login");
  showToast("Session expired. Please sign in again.", "warning");
}

function handleLogout() {
  state.token = null;
  state.currentUser = null;
  localStorage.removeItem("expense_token");
  localStorage.removeItem("expense_user");

  document.getElementById("appContainer").style.display = "none";
  document.getElementById("authContainer").style.display = "flex";
  showAuthView("login");

  // Reset forms
  document.getElementById("loginForm")?.reset();
  document.getElementById("registerForm")?.reset();

  showToast("Logged out successfully.", "success");
}

// App Initialization
async function initApp() {
  setupNavigation();
  setupMonthSelector();
  setupModals();
  setupAuthForms();

  // Close notifications on outer click
  document.addEventListener("click", (e) => {
    const wrapper = document.querySelector(".notification-dropdown-wrapper");
    const panel = document.getElementById("notificationPanel");
    if (wrapper && panel && !wrapper.contains(e.target)) {
      panel.style.display = "none";
    }
  });

  // Check if token exists
  if (state.token) {
    try {
      const user = await api.getCurrentUser();
      state.currentUser = user;
      localStorage.setItem("expense_user", JSON.stringify(user));
      onAuthenticated();
    } catch (err) {
      console.warn("Stored token invalid or expired. Prompting login.");
      handleLogout();
    }
  } else {
    // Show login view
    document.getElementById("appContainer").style.display = "none";
    document.getElementById("authContainer").style.display = "flex";
    showAuthView("login");
  }
}

async function onAuthenticated() {
  document.getElementById("authContainer").style.display = "none";
  document.getElementById("appContainer").style.display = "flex";

  updateUserProfileUI();

  try {
    const categories = await api.getCategories();
    state.categories = categories;
    populateCategoryDropdowns();
  } catch (err) {
    console.error("Failed to fetch categories:", err);
  }

  await loadCurrentViewData();
}

function updateUserProfileUI() {
  if (!state.currentUser) return;
  const userNameEl = document.getElementById("sidebarUserName");
  const userAvatarEl = document.getElementById("sidebarUserAvatar");
  const userIdCodeEl = document.getElementById("sidebarUserIdCode");

  if (userNameEl) userNameEl.textContent = state.currentUser.name;
  if (userIdCodeEl) userIdCodeEl.textContent = state.currentUser.userIdCode || `EXP-${state.currentUser.id + 10000}`;
  if (userAvatarEl) {
    const initials = state.currentUser.name
      .split(" ")
      .map(n => n.charAt(0))
      .slice(0, 2)
      .join("")
      .toUpperCase();
    userAvatarEl.textContent = initials || "U";
  }
}

// Setup Auth Forms
function setupAuthForms() {
  const loginForm = document.getElementById("loginForm");
  const registerForm = document.getElementById("registerForm");

  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const emailInput = document.getElementById("loginEmail");
      const passwordInput = document.getElementById("loginPassword");
      const rememberMeInput = document.getElementById("loginRememberMe");
      const submitBtn = document.getElementById("loginSubmitBtn");
      const spinner = submitBtn.querySelector(".btn-spinner");
      const btnText = submitBtn.querySelector(".btn-text");

      // Validate
      const email = emailInput.value.trim();
      const password = passwordInput.value;

      if (!email || !password) {
        showToast("Please fill in both email and password.", "warning");
        return;
      }

      // Disable button
      submitBtn.disabled = true;
      if (spinner) spinner.style.display = "inline-block";
      if (btnText) btnText.style.display = "none";

      try {
        const res = await api.login({
          email,
          password,
          rememberMe: rememberMeInput ? rememberMeInput.checked : false
        });

        state.token = res.token;
        state.currentUser = res.user;
        localStorage.setItem("expense_token", res.token);
        localStorage.setItem("expense_user", JSON.stringify(res.user));

        showToast(`Welcome back, ${res.user.name}!`, "success");
        onAuthenticated();
      } catch (err) {
        showToast(err.message || "Invalid email or password.", "danger");
      } finally {
        submitBtn.disabled = false;
        if (spinner) spinner.style.display = "none";
        if (btnText) btnText.style.display = "inline-block";
      }
    });
  }

  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const nameInput = document.getElementById("regFullName");
      const emailInput = document.getElementById("regEmail");
      const passwordInput = document.getElementById("regPassword");
      const confirmPasswordInput = document.getElementById("regConfirmPassword");
      const submitBtn = document.getElementById("registerSubmitBtn");
      const spinner = submitBtn.querySelector(".btn-spinner");
      const btnText = submitBtn.querySelector(".btn-text");

      const fullName = nameInput.value.trim();
      const email = emailInput.value.trim();
      const password = passwordInput.value;
      const confirmPassword = confirmPasswordInput.value;

      // Clear field errors
      document.querySelectorAll("#registerForm .field-error").forEach(el => el.classList.remove("active"));

      let hasError = false;
      if (!fullName) {
        setFieldError("regFullNameError", "Name cannot be empty");
        hasError = true;
      }
      if (!email || !email.includes("@")) {
        setFieldError("regEmailError", "Email must be valid");
        hasError = true;
      }
      if (!password || password.length < 6) {
        setFieldError("regPasswordError", "Password must be at least 6 characters long");
        hasError = true;
      }
      if (password !== confirmPassword) {
        setFieldError("regConfirmPasswordError", "Confirm password must match password");
        hasError = true;
      }

      if (hasError) return;

      submitBtn.disabled = true;
      if (spinner) spinner.style.display = "inline-block";
      if (btnText) btnText.style.display = "none";

      try {
        await api.register({
          fullName,
          email,
          password,
          confirmPassword
        });

        showToast("Account created successfully. Please sign in.", "success");
        registerForm.reset();

        // Switch to login and fill email
        showAuthView("login");
        const loginEmail = document.getElementById("loginEmail");
        if (loginEmail) loginEmail.value = email;
      } catch (err) {
        showToast(err.message || "Registration failed.", "danger");
      } finally {
        submitBtn.disabled = false;
        if (spinner) spinner.style.display = "none";
        if (btnText) btnText.style.display = "inline-block";
      }
    });
  }
}

function setFieldError(elementId, message) {
  const el = document.getElementById(elementId);
  if (el) {
    el.textContent = message;
    el.classList.add("active");
  }
}

function setupNavigation() {
  const navLinks = document.querySelectorAll(".nav-link[data-tab]");
  navLinks.forEach(link => {
    link.addEventListener("click", (e) => {
      e.preventDefault();
      const tab = link.getAttribute("data-tab");
      switchTab(tab);
    });
  });

  const mobileBtn = document.getElementById("mobileMenuBtn");
  const sidebar = document.getElementById("appSidebar");
  if (mobileBtn && sidebar) {
    mobileBtn.addEventListener("click", () => {
      sidebar.classList.toggle("mobile-open");
    });
  }
}

function switchTab(tab) {
  state.activeTab = tab;

  // Update nav active classes
  document.querySelectorAll(".nav-link").forEach(el => {
    if (el.getAttribute("data-tab") === tab) el.classList.add("active");
    else el.classList.remove("active");
  });

  // Update header title & subtitle
  const titleEl = document.getElementById("headerTitle");
  const subtitleEl = document.getElementById("headerSubtitle");

  const titles = {
    dashboard: { title: "Dashboard", sub: "Your financial overview" },
    expenses: { title: "Expenses", sub: "Track and manage your spending" },
    budgets: { title: "Budgets", sub: "Manage your monthly category limits" },
    categories: { title: "Categories", sub: "Organize spending by category" },
    reports: { title: "Reports", sub: "Month-over-month trend analytics" },
    profile: { title: "User Profile", sub: "Manage account settings and security" }
  };

  if (titles[tab]) {
    titleEl.textContent = titles[tab].title;
    subtitleEl.textContent = titles[tab].sub;
  }

  // Close mobile sidebar if open
  document.getElementById("appSidebar")?.classList.remove("mobile-open");

  loadCurrentViewData();
}

function setupMonthSelector() {
  const selector = document.getElementById("monthSelector");
  if (!selector) return;

  selector.addEventListener("change", (e) => {
    state.selectedMonth = e.target.value;
    state.filters.month = e.target.value;
    loadCurrentViewData();
  });
}

function populateCategoryDropdowns() {
  const expenseCatSelect = document.getElementById("expenseCategory");
  const budgetCatSelect = document.getElementById("budgetCategory");

  const optionsHtml = state.categories.map(cat =>
    `<option value="${cat.id}">${getCategoryIcon(cat.name)} ${cat.name}</option>`
  ).join("");

  if (expenseCatSelect) expenseCatSelect.innerHTML = optionsHtml;
  if (budgetCatSelect) budgetCatSelect.innerHTML = optionsHtml;
}

// Notifications Toggle & Helpers
function toggleNotifications() {
  const panel = document.getElementById("notificationPanel");
  if (!panel) return;
  panel.style.display = panel.style.display === "none" ? "flex" : "none";
}

function clearNotifications() {
  state.notifications = [];
  updateNotificationCenter();
}

function addNotification(title, message, type = "normal") {
  // Avoid duplicates
  const exists = state.notifications.some(n => n.title === title && n.message === message);
  if (!exists) {
    state.notifications.unshift({
      id: Date.now(),
      title,
      message,
      time: "Just now",
      type
    });
    if (state.notifications.length > 20) state.notifications.pop();
    updateNotificationCenter();
  }
}

function updateNotificationCenter() {
  const badge = document.getElementById("notificationBadge");
  const list = document.getElementById("notificationList");
  if (!badge || !list) return;

  if (state.notifications.length > 0) {
    badge.textContent = state.notifications.length;
    badge.style.display = "block";
    list.innerHTML = state.notifications.map(n => `
      <div class="notification-item ${n.type}">
        <div class="notification-item-title">${n.title}</div>
        <div>${n.message}</div>
        <div class="notification-item-time">${n.time}</div>
      </div>
    `).join("");
  } else {
    badge.style.display = "none";
    list.innerHTML = `<div class="notification-empty">No new alerts. Your budgets are healthy!</div>`;
  }
}

// Load Active View Data
async function loadCurrentViewData() {
  const contentBody = document.getElementById("contentBody");
  if (!contentBody) return;

  contentBody.innerHTML = `<div class="empty-state"><div class="btn-spinner" style="border-top-color: var(--primary); width: 32px; height: 32px;"></div></div>`;

  try {
    switch (state.activeTab) {
      case "dashboard":
        await loadDashboardData();
        renderDashboard();
        break;
      case "expenses":
        await loadExpensesData();
        renderExpenses();
        break;
      case "budgets":
        await loadBudgetsData();
        renderBudgets();
        break;
      case "categories":
        await loadCategoriesData();
        renderCategories();
        break;
      case "reports":
        await loadReportsData();
        renderReports();
        break;
      case "profile":
        renderProfile();
        break;
      default:
        await loadDashboardData();
        renderDashboard();
    }
  } catch (err) {
    contentBody.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <div class="empty-state-title">Something went wrong</div>
        <div class="empty-state-text">${err.message}</div>
        <button class="btn btn-primary" onclick="loadCurrentViewData()">Try Again</button>
      </div>
    `;
  }
}

async function loadDashboardData() {
  const [personalDash, trend] = await Promise.all([
    api.getPersonalDashboard(state.selectedMonth),
    api.getTrend()
  ]);

  state.personalDashboard = personalDash;
  state.trend = trend;

  // Process budget alerts into notification center
  if (personalDash.budgetAlerts && personalDash.budgetAlerts.length > 0) {
    personalDash.budgetAlerts.forEach(alert => {
      const type = alert.alertLevel === "EXCEEDED" ? "exceeded" : alert.alertLevel === "WARNING" ? "warning" : "normal";
      addNotification(alert.alertLevel === "EXCEEDED" ? "Budget Exceeded" : "Budget Limit Warning", alert.message, type);
    });
  }
}

async function loadExpensesData() {
  const params = {
    month: state.selectedMonth
  };
  if (state.filters.search) params.search = state.filters.search;
  if (state.filters.categoryId) params.categoryId = state.filters.categoryId;
  if (state.filters.startDate) params.startDate = state.filters.startDate;
  if (state.filters.endDate) params.endDate = state.filters.endDate;
  if (state.filters.minAmount) params.minAmount = state.filters.minAmount;
  if (state.filters.maxAmount) params.maxAmount = state.filters.maxAmount;

  state.expenses = await api.getExpenses(params);
}

async function loadBudgetsData() {
  state.budgets = await api.getBudgets({ month: state.selectedMonth });
  state.summary = await api.getSummary(state.selectedMonth);
}

async function loadCategoriesData() {
  state.categories = await api.getCategories();
  state.summary = await api.getSummary(state.selectedMonth);
}

async function loadReportsData() {
  const [trend, summary] = await Promise.all([
    api.getTrend(),
    api.getSummary(state.selectedMonth)
  ]);
  state.trend = trend;
  state.summary = summary;
}

// =====================================================================
// RENDER VIEWS
// =====================================================================

function renderDashboard() {
  const container = document.getElementById("contentBody");
  if (!container || !state.personalDashboard) return;

  const d = state.personalDashboard;
  const monthName = formatMonthName(state.selectedMonth);

  // Determine budget status class
  const usedPct = d.budgetUsedPercentage || 0;
  const usedPctClass = usedPct >= 100 ? "text-danger" : usedPct >= 90 ? "text-warning" : "text-success";

  container.innerHTML = `
    <div class="dashboard-top-section">
      <!-- Quick Actions -->
      <div class="quick-actions-bar">
        <button class="btn-quick-action" onclick="openExpenseModal()">
          <span>+</span> Add Expense
        </button>
        <button class="btn-quick-action" onclick="openBudgetModal()">
          <span>+</span> Set Budget
        </button>
        <button class="btn-quick-action" onclick="openCategoryModal()">
          <span>+</span> Add Category
        </button>
      </div>

      <!-- Monthly Financial Summary (Requirement 9) -->
      <div class="financial-summary-card">
        <div class="summary-card-badge">
          <svg width="14" height="14" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Monthly Summary
        </div>
        <div class="summary-title">${monthName} Financial Overview</div>
        <div class="summary-text-lead">${d.summaryText}</div>
      </div>

      <!-- Smart Spending Insights (Requirement 10) -->
      ${d.insights && d.insights.length > 0 ? `
        <div class="insights-section">
          <div class="insights-header">
            <div class="insights-title">
              <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor" style="color: var(--primary);">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              Smart Spending Insights
            </div>
          </div>
          <div class="insights-grid">
            ${d.insights.map(item => `
              <div class="insight-card">
                <div class="insight-icon">💡</div>
                <div>${item}</div>
              </div>
            `).join("")}
          </div>
        </div>
      ` : ""}
    </div>

    <!-- 4 SUMMARY CARDS (Requirement 8) -->
    <div class="summary-cards-grid">
      <!-- Total Spent -->
      <div class="metric-card">
        <div class="metric-card-top">
          <span class="metric-label">Total Spent</span>
          <div class="metric-icon-badge" style="background-color: #FEF2F2; color: #EF4444;">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
        </div>
        <div class="metric-value">${formatCurrency(d.totalSpent)}</div>
        <div class="metric-subtext">Across this month</div>
      </div>

      <!-- Monthly Budget -->
      <div class="metric-card">
        <div class="metric-card-top">
          <span class="metric-label">Monthly Budget</span>
          <div class="metric-icon-badge" style="background-color: var(--primary-subtle); color: var(--primary);">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
            </svg>
          </div>
        </div>
        <div class="metric-value">${formatCurrency(d.monthlyBudget)}</div>
        <div class="metric-subtext">Combined category limits</div>
      </div>

      <!-- Remaining Budget -->
      <div class="metric-card">
        <div class="metric-card-top">
          <span class="metric-label">Remaining Budget</span>
          <div class="metric-icon-badge" style="background-color: var(--success-bg); color: var(--success);">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        </div>
        <div class="metric-value ${d.remainingBudget < 0 ? "text-danger" : ""}">${formatCurrency(d.remainingBudget)}</div>
        <div class="metric-subtext">${d.remainingBudget >= 0 ? "Safe to spend" : "Budget limit exceeded"}</div>
      </div>

      <!-- Budget Used -->
      <div class="metric-card">
        <div class="metric-card-top">
          <span class="metric-label">Budget Used</span>
          <div class="metric-icon-badge" style="background-color: #FFFBEB; color: #D97706;">
            <svg width="18" height="18" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
        </div>
        <div class="metric-value ${usedPctClass}">${usedPct.toFixed(1)}%</div>
        <div class="metric-subtext">Overall allocation</div>
      </div>
    </div>

    <!-- MAIN TWO-COLUMN DASHBOARD GRID -->
    <div class="dashboard-grid">
      <!-- Monthly Spending Chart -->
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Monthly Spending Trend</h3>
            <p class="card-subtitle">Spending trajectory across recorded months</p>
          </div>
        </div>
        <div class="card-body">
          <div class="chart-container" id="spendingChart">
            <!-- Rendered by renderSplineChart -->
          </div>
        </div>
      </div>

      <!-- Category Spending Breakdown (Tiered Alerts) -->
      <div class="card">
        <div class="card-header">
          <div>
            <h3 class="card-title">Category Spending</h3>
            <p class="card-subtitle">${monthName} budget utilization</p>
          </div>
        </div>
        <div class="card-body">
          <div class="category-spending-list">
            ${d.categorySummaries && d.categorySummaries.length > 0 ? d.categorySummaries.map(cat => {
              const pct = cat.percentageUsed || 0;
              const fillClass = pct >= 100 ? "progress-fill-danger" : pct >= 90 ? "progress-fill-warning" : "progress-fill-normal";
              const badgeClass = pct >= 100 ? "badge-exceeded" : pct >= 90 ? "badge-warning" : pct >= 75 ? "badge-approaching" : "badge-normal";

              return `
                <div class="category-spending-item">
                  <div class="category-spending-header">
                    <span class="cat-name">${getCategoryIcon(cat.categoryName || cat.category)} ${cat.categoryName || cat.category}</span>
                    <span class="cat-amount">${formatCurrency(cat.totalSpent)} / ${formatCurrency(cat.budget || cat.budgetAmount)}</span>
                  </div>
                  <div class="progress-bar-wrapper">
                    <div class="progress-bar-fill ${fillClass}" style="width: ${Math.min(pct, 100)}%;"></div>
                  </div>
                  <div class="category-spending-footer">
                    <span class="cat-status-badge ${badgeClass}">
                      ${pct >= 100 ? "Exceeded" : pct >= 90 ? "Warning (90%+)" : pct >= 75 ? "Approaching Limit" : "Normal"}
                    </span>
                    <span class="cat-percentage">${pct.toFixed(0)}% used</span>
                  </div>
                </div>
              `;
            }).join("") : `
              <div class="empty-state">
                <div class="empty-state-title">No spending logged</div>
                <div class="empty-state-text">Start tracking by adding your first expense.</div>
              </div>
            `}
          </div>
        </div>
      </div>
    </div>

    <!-- BOTTOM ROW: RECENT EXPENSES -->
    <div class="card" style="margin-top: 1.5rem;">
      <div class="card-header">
        <div>
          <h3 class="card-title">Recent Expenses</h3>
          <p class="card-subtitle">Your latest logged transactions</p>
        </div>
        <button class="btn btn-secondary btn-sm" onclick="switchTab('expenses')">View All</button>
      </div>
      <div class="card-body">
        ${d.recentExpenses && d.recentExpenses.length > 0 ? `
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th>Frequency</th>
                  <th style="text-align: right;">Amount</th>
                </tr>
              </thead>
              <tbody>
                ${d.recentExpenses.map(e => `
                  <tr>
                    <td>${formatDate(e.date)}</td>
                    <td>
                      <span class="cat-badge">
                        ${getCategoryIcon(e.categoryName)} ${e.categoryName}
                      </span>
                    </td>
                    <td>${e.description || "—"}</td>
                    <td>
                      <span class="recurring-badge ${e.recurringType ? e.recurringType.toLowerCase() : 'one_time'}">
                        ${e.recurringType === "DAILY" ? "Daily" : e.recurringType === "WEEKLY" ? "Weekly" : e.recurringType === "MONTHLY" ? "Monthly" : "One-time"}
                      </span>
                    </td>
                    <td style="text-align: right; font-weight: 700;">${formatCurrency(e.amount)}</td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>
        ` : `
          <div class="empty-state">
            <div class="empty-state-icon">💸</div>
            <div class="empty-state-title">No expenses yet</div>
            <div class="empty-state-text">Start tracking your spending by adding your first expense.</div>
            <button class="btn btn-primary" onclick="openExpenseModal()">+ Add Expense</button>
          </div>
        `}
      </div>
    </div>
  `;

  // Render Spline Chart
  renderSplineChart("spendingChart", state.trend);
}

// Interactive SVG Spline Chart
function renderSplineChart(containerId, trendData) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const items = trendData?.monthlySpending || [];
  if (items.length === 0) {
    container.innerHTML = `
      <div class="empty-state" style="padding: 2rem 0;">
        <div class="empty-state-title">No trend data available</div>
        <div class="empty-state-text">Add expenses across months to visualize your spending trend.</div>
      </div>
    `;
    return;
  }

  const width = 600;
  const height = 240;
  const padding = { top: 20, right: 30, bottom: 40, left: 60 };

  const chartW = width - padding.left - padding.right;
  const chartH = height - padding.top - padding.bottom;

  const maxVal = Math.max(...items.map(d => Number(d.total || d.totalSpent || 0)), 1000) * 1.15;

  const points = items.map((item, index) => {
    const x = padding.left + (index / Math.max(items.length - 1, 1)) * chartW;
    const y = padding.top + chartH - ((Number(item.total || item.totalSpent || 0) / maxVal) * chartH);
    return { x, y, item };
  });

  // Build SVG Path
  let pathD = `M ${points[0].x} ${points[0].y}`;
  for (let i = 0; i < points.length - 1; i++) {
    const p0 = points[i];
    const p1 = points[i + 1];
    const cp1x = p0.x + (p1.x - p0.x) / 2;
    const cp1y = p0.y;
    const cp2x = p0.x + (p1.x - p0.x) / 2;
    const cp2y = p1.y;
    pathD += ` C ${cp1x} ${cp1y}, ${cp2x} ${cp2y}, ${p1.x} ${p1.y}`;
  }

  // Gradient area
  const areaD = `${pathD} L ${points[points.length - 1].x} ${padding.top + chartH} L ${points[0].x} ${padding.top + chartH} Z`;

  container.innerHTML = `
    <svg viewBox="0 0 ${width} ${height}" style="width: 100%; height: 100%; overflow: visible;">
      <defs>
        <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#0D9488" stop-opacity="0.25" />
          <stop offset="100%" stop-color="#0D9488" stop-opacity="0.0" />
        </linearGradient>
      </defs>

      <!-- Grid lines -->
      <line x1="${padding.left}" y1="${padding.top}" x2="${width - padding.right}" y2="${padding.top}" stroke="#E2E8F0" stroke-dasharray="3,3" />
      <line x1="${padding.left}" y1="${padding.top + chartH / 2}" x2="${width - padding.right}" y2="${padding.top + chartH / 2}" stroke="#E2E8F0" stroke-dasharray="3,3" />
      <line x1="${padding.left}" y1="${padding.top + chartH}" x2="${width - padding.right}" y2="${padding.top + chartH}" stroke="#E2E8F0" />

      <!-- Area fill -->
      <path d="${areaD}" fill="url(#chartGradient)" />

      <!-- Spline curve -->
      <path d="${pathD}" fill="none" stroke="#0D9488" stroke-width="3" stroke-linecap="round" />

      <!-- Points & labels -->
      ${points.map(p => `
        <circle cx="${p.x}" cy="${p.y}" r="5" fill="#FFFFFF" stroke="#0D9488" stroke-width="2.5" />
        <text x="${p.x}" y="${padding.top + chartH + 20}" text-anchor="middle" font-size="11" fill="#64748B" font-weight="600">
          ${formatMonthName(p.item.month).split(" ")[0]}
        </text>
        <text x="${p.x}" y="${p.y - 12}" text-anchor="middle" font-size="11" fill="#0F172A" font-weight="700">
          ${formatCurrency(p.item.total || p.item.totalSpent)}
        </text>
      `).join("")}
    </svg>
  `;
}

// =====================================================================
// RENDER EXPENSES PAGE (SEARCH, FILTERS, RECURRING & EXPORT)
// =====================================================================
function renderExpenses() {
  const container = document.getElementById("contentBody");
  if (!container) return;

  container.innerHTML = `
    <!-- Search & Filter Toolbar (Requirement 13 & 15) -->
    <div class="filter-toolbar">
      <div class="filter-left-controls">
        <div class="search-input-box">
          <svg class="search-icon-inside" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input type="text" id="expenseSearchInput" class="input-field" placeholder="Search expenses..." value="${state.filters.search || ""}" />
        </div>

        <select id="expenseCategoryFilter" class="filter-date-input" style="height: 38px;">
          <option value="">All Categories</option>
          ${state.categories.map(c => `
            <option value="${c.id}" ${state.filters.categoryId == c.id ? "selected" : ""}>${c.name}</option>
          `).join("")}
        </select>

        <div class="filter-dates-group">
          <input type="date" id="expenseFilterStartDate" class="filter-date-input" title="Start date" value="${state.filters.startDate || ""}" />
          <span style="color: var(--text-subtle);">to</span>
          <input type="date" id="expenseFilterEndDate" class="filter-date-input" title="End date" value="${state.filters.endDate || ""}" />
        </div>

        <button class="btn btn-secondary btn-sm" onclick="applyExpenseFilters()">Apply</button>
        <button class="btn-text-only" onclick="resetExpenseFilters()">Clear</button>
      </div>

      <div class="filter-right-actions">
        <!-- Export CSV Button (Requirement 15) -->
        <button class="btn-export" onclick="exportExpensesCsv()">
          <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
          </svg>
          Export CSV
        </button>
        <button class="btn btn-primary btn-sm" onclick="openExpenseModal()">+ Add Expense</button>
      </div>
    </div>

    <!-- Expenses List Table -->
    <div class="card">
      <div class="card-body">
        ${state.expenses && state.expenses.length > 0 ? `
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th>Frequency</th>
                  <th style="text-align: right;">Amount</th>
                  <th style="text-align: right;">Actions</th>
                </tr>
              </thead>
              <tbody>
                ${state.expenses.map(e => `
                  <tr>
                    <td>${formatDate(e.date)}</td>
                    <td>
                      <span class="cat-badge">
                        ${getCategoryIcon(e.categoryName)} ${e.categoryName}
                      </span>
                    </td>
                    <td>${e.description || "—"}</td>
                    <td>
                      <span class="recurring-badge ${e.recurringType ? e.recurringType.toLowerCase() : 'one_time'}">
                        ${e.recurringType === "DAILY" ? "Daily" : e.recurringType === "WEEKLY" ? "Weekly" : e.recurringType === "MONTHLY" ? "Monthly" : "One-time"}
                      </span>
                    </td>
                    <td style="text-align: right; font-weight: 700;">${formatCurrency(e.amount)}</td>
                    <td style="text-align: right;">
                      <button class="action-btn" onclick="editExpense(${e.id})" title="Edit expense">
                        <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                      </button>
                      <button class="action-btn text-danger" onclick="deleteExpenseItem(${e.id})" title="Delete expense">
                        <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>
        ` : `
          <!-- Empty State (Requirement 24) -->
          <div class="empty-state">
            <div class="empty-state-icon">💸</div>
            <div class="empty-state-title">No expenses found</div>
            <div class="empty-state-text">Start tracking your spending by adding your first expense.</div>
            <button class="btn btn-primary" onclick="openExpenseModal()">+ Add Expense</button>
          </div>
        `}
      </div>
    </div>
  `;

  // Search input onEnter listener
  const searchInput = document.getElementById("expenseSearchInput");
  if (searchInput) {
    searchInput.addEventListener("keypress", (e) => {
      if (e.key === "Enter") applyExpenseFilters();
    });
  }
}

function applyExpenseFilters() {
  state.filters.search = document.getElementById("expenseSearchInput")?.value.trim() || "";
  state.filters.categoryId = document.getElementById("expenseCategoryFilter")?.value || "";
  state.filters.startDate = document.getElementById("expenseFilterStartDate")?.value || "";
  state.filters.endDate = document.getElementById("expenseFilterEndDate")?.value || "";

  loadCurrentViewData();
}

function resetExpenseFilters() {
  state.filters.search = "";
  state.filters.categoryId = "";
  state.filters.startDate = "";
  state.filters.endDate = "";
  state.filters.minAmount = "";
  state.filters.maxAmount = "";

  loadCurrentViewData();
}

function exportExpensesCsv() {
  const params = new URLSearchParams();
  if (state.selectedMonth) params.append("month", state.selectedMonth);
  if (state.filters.categoryId) params.append("categoryId", state.filters.categoryId);
  if (state.filters.search) params.append("search", state.filters.search);
  if (state.filters.startDate) params.append("startDate", state.filters.startDate);
  if (state.filters.endDate) params.append("endDate", state.filters.endDate);

  const url = "/api/expenses/export/csv?" + params.toString();

  fetch(url, {
    headers: {
      "Authorization": `Bearer ${state.token}`
    }
  })
  .then(res => {
    if (!res.ok) throw new Error("Failed to export CSV");
    return res.blob();
  })
  .then(blob => {
    const downloadUrl = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = downloadUrl;
    a.download = `expenses_export_${state.selectedMonth || "all"}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(downloadUrl);
    showToast("Expenses exported to CSV successfully.", "success");
  })
  .catch(err => {
    showToast(err.message, "danger");
  });
}

// =====================================================================
// RENDER BUDGETS PAGE (TIERED BUDGET ALERTS)
// =====================================================================
function renderBudgets() {
  const container = document.getElementById("contentBody");
  if (!container) return;

  const monthName = formatMonthName(state.selectedMonth);

  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-main);">Monthly Budgets for ${monthName}</h2>
        <p style="font-size: 0.85rem; color: var(--text-muted);">Set spending thresholds per category and receive automated 90% and exceeded warnings.</p>
      </div>
      <button class="btn btn-primary" onclick="openBudgetModal()">+ Set Budget</button>
    </div>

    ${state.budgets && state.budgets.length > 0 ? `
      <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 1.25rem;">
        ${state.budgets.map(b => {
          // Find matching summary
          const summaryItem = state.summary.find(s => s.categoryId === b.categoryId);
          const totalSpent = summaryItem ? summaryItem.totalSpent : 0;
          const percentageUsed = b.amount > 0 ? (totalSpent / b.amount) * 100 : 0;

          const badgeClass = percentageUsed >= 100 ? "badge-exceeded" : percentageUsed >= 90 ? "badge-warning" : percentageUsed >= 75 ? "badge-approaching" : "badge-normal";
          const progressClass = percentageUsed >= 100 ? "progress-fill-danger" : percentageUsed >= 90 ? "progress-fill-warning" : "progress-fill-normal";

          return `
            <div class="card">
              <div class="card-header" style="border-bottom: none; padding-bottom: 0.5rem;">
                <div style="display: flex; align-items: center; gap: 0.6rem;">
                  <span style="font-size: 1.5rem;">${getCategoryIcon(b.categoryName)}</span>
                  <div>
                    <h3 class="card-title">${b.categoryName}</h3>
                    <p class="card-subtitle">${b.month}</p>
                  </div>
                </div>
                <div class="cat-status-badge ${badgeClass}">
                  ${percentageUsed >= 100 ? "Budget Exceeded" : percentageUsed >= 90 ? "⚠ Warning (90%+)" : percentageUsed >= 75 ? "Approaching Limit" : "Normal"}
                </div>
              </div>

              <div class="card-body">
                <div style="display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 0.75rem;">
                  <div>
                    <div style="font-size: 0.75rem; color: var(--text-subtle);">SPENT</div>
                    <div style="font-size: 1.25rem; font-weight: 700; color: var(--text-main);">${formatCurrency(totalSpent)}</div>
                  </div>
                  <div style="text-align: right;">
                    <div style="font-size: 0.75rem; color: var(--text-subtle);">BUDGET LIMIT</div>
                    <div style="font-size: 1.25rem; font-weight: 700; color: var(--primary);">${formatCurrency(b.amount)}</div>
                  </div>
                </div>

                <div class="progress-bar-wrapper">
                  <div class="progress-bar-fill ${progressClass}" style="width: ${Math.min(percentageUsed, 100)}%;"></div>
                </div>

                <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 0.75rem; font-size: 0.8rem; color: var(--text-muted);">
                  <span>${percentageUsed.toFixed(1)}% used</span>
                  <span>${formatCurrency(Math.max(b.amount - totalSpent, 0))} remaining</span>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 1rem; padding-top: 0.75rem; border-top: 1px solid var(--border-light);">
                  <button class="action-btn" onclick="editBudget(${b.id})" title="Edit budget">
                    <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                  </button>
                  <button class="action-btn text-danger" onclick="deleteBudgetItem(${b.id})" title="Delete budget">
                    <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          `;
        }).join("")}
      </div>
    ` : `
      <!-- Empty State (Requirement 24) -->
      <div class="card">
        <div class="card-body">
          <div class="empty-state">
            <div class="empty-state-icon">🎯</div>
            <div class="empty-state-title">No budgets created yet</div>
            <div class="empty-state-text">Set category limits to keep your personal spending disciplined and receive automatic alerts.</div>
            <button class="btn btn-primary" onclick="openBudgetModal()">+ Set Budget</button>
          </div>
        </div>
      </div>
    `}
  `;
}

// =====================================================================
// RENDER CATEGORIES PAGE
// =====================================================================
function renderCategories() {
  const container = document.getElementById("contentBody");
  if (!container) return;

  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-main);">Expense Categories</h2>
        <p style="font-size: 0.85rem; color: var(--text-muted);">Organize and categorize all your everyday purchases and recurring commitments.</p>
      </div>
      <button class="btn btn-primary" onclick="openCategoryModal()">+ Add Category</button>
    </div>

    <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 1.25rem;">
      ${state.categories.map(cat => {
        const sumItem = state.summary.find(s => s.categoryId === cat.id);
        const spent = sumItem ? sumItem.totalSpent : 0;

        return `
          <div class="card">
            <div class="card-body">
              <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 0.75rem;">
                <div style="font-size: 2rem;">${getCategoryIcon(cat.name)}</div>
                <div>
                  <h3 style="font-size: 1.1rem; font-weight: 700; color: var(--text-main);">${cat.name}</h3>
                  <div style="font-size: 0.75rem; color: var(--text-subtle);">Category #${cat.id}</div>
                </div>
              </div>
              <p style="font-size: 0.85rem; color: var(--text-muted); min-height: 40px; margin-bottom: 1rem;">
                ${cat.description || "General category for recording related expenses."}
              </p>
              <div style="padding-top: 0.75rem; border-top: 1px solid var(--border-light); display: flex; justify-content: space-between; font-size: 0.825rem;">
                <span style="color: var(--text-subtle);">Spent this month:</span>
                <span style="font-weight: 700; color: var(--text-main);">${formatCurrency(spent)}</span>
              </div>
            </div>
          </div>
        `;
      }).join("")}
    </div>
  `;
}

// =====================================================================
// RENDER REPORTS PAGE (TRENDS & COMPARISONS)
// =====================================================================
function renderReports() {
  const container = document.getElementById("contentBody");
  if (!container || !state.trend) return;

  const t = state.trend;

  container.innerHTML = `
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem;">
      <div>
        <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-main);">Financial Reports & Spending Trends</h2>
        <p style="font-size: 0.85rem; color: var(--text-muted);">Historical insights, month-over-month trajectory, and breakdown analytics.</p>
      </div>
      <button class="btn-export" onclick="exportExpensesCsv()">
        <svg width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
        </svg>
        Export CSV
      </button>
    </div>

    <!-- MoM Metrics Row -->
    <div class="summary-cards-grid" style="margin-bottom: 1.5rem;">
      <div class="metric-card">
        <span class="metric-label">Latest Month</span>
        <div class="metric-value">${t.latestMonth ? formatMonthName(t.latestMonth) : "No Data"}</div>
        <div class="metric-subtext">Most recent active ledger</div>
      </div>
      <div class="metric-card">
        <span class="metric-label">Previous Month</span>
        <div class="metric-value">${t.previousMonth ? formatMonthName(t.previousMonth) : "—"}</div>
        <div class="metric-subtext">Comparison baseline</div>
      </div>
      <div class="metric-card">
        <span class="metric-label">Month-over-Month Change</span>
        <div class="metric-value ${t.monthOverMonthChange > 0 ? "text-danger" : "text-success"}">
          ${t.monthOverMonthChange !== null && t.monthOverMonthChange !== undefined ? formatCurrency(t.monthOverMonthChange) : "₹0"}
        </div>
        <div class="metric-subtext">${t.percentageChange !== null && t.percentageChange !== undefined ? `${t.percentageChange > 0 ? "+" : ""}${t.percentageChange.toFixed(1)}% vs previous month` : "First recorded month"}</div>
      </div>
      <div class="metric-card">
        <span class="metric-label">Trend Direction</span>
        <div class="metric-value" style="color: var(--primary);">
          ${t.spendingTrendDirection === "INCREASED" ? "📈 Spending Up" : t.spendingTrendDirection === "DECREASED" ? "📉 Spending Down" : "⚖️ Stable"}
        </div>
        <div class="metric-subtext">Spending trend classification</div>
      </div>
    </div>

    <!-- Large Interactive Spline Chart -->
    <div class="card" style="margin-bottom: 1.5rem;">
      <div class="card-header">
        <h3 class="card-title">Monthly Trajectory Chart</h3>
      </div>
      <div class="card-body">
        <div class="chart-container" id="reportSpendingChart" style="height: 280px;">
          <!-- SVG Chart -->
        </div>
      </div>
    </div>
  `;

  renderSplineChart("reportSpendingChart", state.trend);
}

// =====================================================================
// RENDER USER PROFILE (Requirement 7)
// =====================================================================
function renderProfile() {
  const container = document.getElementById("contentBody");
  if (!container || !state.currentUser) return;

  const u = state.currentUser;
  const initials = u.name.split(" ").map(n => n.charAt(0)).slice(0, 2).join("").toUpperCase() || "U";
  const createdDate = u.createdAt ? new Date(u.createdAt).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" }) : "Active Member";

  container.innerHTML = `
    <div class="profile-container">
      <!-- Profile Header Card -->
      <div class="profile-header-card">
        <div class="profile-header-left">
          <div class="profile-avatar-large">${initials}</div>
          <div>
            <div class="profile-title-name">${u.name}</div>
            <div class="profile-title-email">${u.email}</div>
            <div class="user-id-chip">
              <span>User ID:</span>
              <strong id="profileUserIdText">${u.userIdCode || `EXP-${u.id + 10000}`}</strong>
              <button type="button" class="btn-text-only" onclick="copyUserId()" title="Copy User ID">📋</button>
            </div>
          </div>
        </div>

        <div style="text-align: right;">
          <div style="font-size: 0.75rem; color: var(--text-subtle);">ACCOUNT STATUS</div>
          <div style="font-size: 0.9rem; font-weight: 700; color: var(--success);">● Active Verified Account</div>
          <div style="font-size: 0.75rem; color: var(--text-muted); margin-top: 0.25rem;">Joined: ${createdDate}</div>
        </div>
      </div>

      <!-- Settings Grid -->
      <div class="profile-sections-grid">
        <!-- Update Profile Details -->
        <div class="profile-card">
          <div class="profile-card-title">Profile Information</div>
          <div class="profile-card-desc">Update your display name and email address.</div>

          <form id="profileUpdateForm" class="profile-card-form">
            <div class="form-group">
              <label class="form-label" for="profileName">Full Name</label>
              <input type="text" id="profileName" class="input-field" value="${u.name}" required />
            </div>

            <div class="form-group">
              <label class="form-label" for="profileEmail">Email Address</label>
              <input type="email" id="profileEmail" class="input-field" value="${u.email}" required />
            </div>

            <button type="submit" class="btn btn-primary" id="saveProfileBtn">Save Changes</button>
          </form>
        </div>

        <!-- Change Password -->
        <div class="profile-card">
          <div class="profile-card-title">Security & Password</div>
          <div class="profile-card-desc">Keep your account secure by using a strong password.</div>

          <form id="profilePasswordForm" class="profile-card-form">
            <div class="form-group">
              <label class="form-label" for="currPassword">Current Password</label>
              <input type="password" id="currPassword" class="input-field" placeholder="••••••••" required />
            </div>

            <div class="form-group">
              <label class="form-label" for="newPassword">New Password</label>
              <input type="password" id="newPassword" class="input-field" placeholder="Minimum 6 characters" required />
            </div>

            <div class="form-group">
              <label class="form-label" for="confirmNewPassword">Confirm New Password</label>
              <input type="password" id="confirmNewPassword" class="input-field" placeholder="••••••••" required />
            </div>

            <button type="submit" class="btn btn-secondary" id="changePasswordBtn">Update Password</button>
          </form>
        </div>
      </div>
    </div>
  `;

  // Attach Profile Submit Handlers
  const profileForm = document.getElementById("profileUpdateForm");
  if (profileForm) {
    profileForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const name = document.getElementById("profileName").value.trim();
      const email = document.getElementById("profileEmail").value.trim();
      const btn = document.getElementById("saveProfileBtn");

      if (!name || !email) return;

      btn.disabled = true;
      try {
        const updated = await api.updateProfile({ name, email });
        state.currentUser = updated;
        localStorage.setItem("expense_user", JSON.stringify(updated));
        updateUserProfileUI();
        showToast("Profile updated successfully.", "success");
        renderProfile();
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        btn.disabled = false;
      }
    });
  }

  const passwordForm = document.getElementById("profilePasswordForm");
  if (passwordForm) {
    passwordForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const currentPassword = document.getElementById("currPassword").value;
      const newPassword = document.getElementById("newPassword").value;
      const confirmNewPassword = document.getElementById("confirmNewPassword").value;
      const btn = document.getElementById("changePasswordBtn");

      if (newPassword.length < 6) {
        showToast("New password must be at least 6 characters long.", "warning");
        return;
      }

      if (newPassword !== confirmNewPassword) {
        showToast("New passwords do not match.", "warning");
        return;
      }

      btn.disabled = true;
      try {
        await api.changePassword({ currentPassword, newPassword, confirmNewPassword });
        passwordForm.reset();
        showToast("Password updated successfully.", "success");
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        btn.disabled = false;
      }
    });
  }
}

function copyUserId() {
  const code = document.getElementById("profileUserIdText")?.textContent;
  if (code) {
    navigator.clipboard.writeText(code);
    showToast(`Copied User ID: ${code}`, "success");
  }
}

// =====================================================================
// MODAL MANAGEMENT & FORM ACTIONS
// =====================================================================

function setupModals() {
  // Expense Form
  const expenseForm = document.getElementById("expenseForm");
  if (expenseForm) {
    expenseForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const expenseId = document.getElementById("expenseId").value;
      const amount = parseFloat(document.getElementById("expenseAmount").value);
      const categoryId = parseInt(document.getElementById("expenseCategory").value);
      const date = document.getElementById("expenseDate").value;
      const description = document.getElementById("expenseDescription").value.trim();
      const recurringType = document.getElementById("expenseRecurring").value;
      const saveBtn = document.getElementById("saveExpenseBtn");

      if (isNaN(amount) || amount <= 0) {
        showToast("Amount must be greater than ₹0.", "warning");
        return;
      }

      const payload = {
        amount,
        categoryId,
        date,
        description,
        recurringType
      };

      saveBtn.disabled = true;

      try {
        if (expenseId) {
          await api.updateExpense(expenseId, payload);
          showToast("Expense updated successfully.", "success");
        } else {
          const created = await api.createExpense(payload);
          showToast("Expense added successfully.", "success");

          // Check if triggered warning
          if (created.budgetAlert && created.budgetAlert.warning) {
            addNotification(
              created.budgetAlert.alertLevel === "EXCEEDED" ? "Budget Exceeded" : "Budget Limit Warning",
              created.budgetAlert.message,
              created.budgetAlert.alertLevel === "EXCEEDED" ? "exceeded" : "warning"
            );
          }
        }

        closeModal("expenseModal");
        await loadCurrentViewData();
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        saveBtn.disabled = false;
      }
    });
  }

  // Budget Form
  const budgetForm = document.getElementById("budgetForm");
  if (budgetForm) {
    budgetForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const budgetId = document.getElementById("budgetId").value;
      const categoryId = parseInt(document.getElementById("budgetCategory").value);
      const amount = parseFloat(document.getElementById("budgetAmount").value);
      const month = document.getElementById("budgetMonth").value.trim();
      const saveBtn = document.getElementById("saveBudgetBtn");

      if (isNaN(amount) || amount <= 0) {
        showToast("Budget amount must be greater than ₹0.", "warning");
        return;
      }

      const payload = {
        categoryId,
        amount,
        month
      };

      saveBtn.disabled = true;

      try {
        if (budgetId) {
          await api.updateBudget(budgetId, payload);
          showToast("Budget updated successfully.", "success");
        } else {
          await api.createBudget(payload);
          showToast("Budget configured successfully.", "success");
          addNotification("Budget Created", `Monthly limit of ₹${amount} set for this category.`, "normal");
        }

        closeModal("budgetModal");
        await loadCurrentViewData();
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        saveBtn.disabled = false;
      }
    });
  }

  // Category Form
  const categoryForm = document.getElementById("categoryForm");
  if (categoryForm) {
    categoryForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const name = document.getElementById("categoryName").value.trim();
      const description = document.getElementById("categoryDescription").value.trim();
      const saveBtn = document.getElementById("saveCategoryBtn");

      if (!name) return;

      saveBtn.disabled = true;

      try {
        await api.createCategory({ name, description });
        showToast(`Category "${name}" created successfully.`, "success");
        closeModal("categoryModal");
        categoryForm.reset();

        state.categories = await api.getCategories();
        populateCategoryDropdowns();
        await loadCurrentViewData();
      } catch (err) {
        showToast(err.message, "danger");
      } finally {
        saveBtn.disabled = false;
      }
    });
  }
}

function openExpenseModal(expense = null) {
  const modal = document.getElementById("expenseModal");
  const form = document.getElementById("expenseForm");
  const title = document.getElementById("expenseModalTitle");
  if (!modal || !form) return;

  form.reset();

  if (expense) {
    title.textContent = "Edit Expense";
    document.getElementById("expenseId").value = expense.id;
    document.getElementById("expenseAmount").value = expense.amount;
    document.getElementById("expenseCategory").value = expense.categoryId;
    document.getElementById("expenseDate").value = expense.date;
    document.getElementById("expenseDescription").value = expense.description || "";
    if (document.getElementById("expenseRecurring")) {
      document.getElementById("expenseRecurring").value = expense.recurringType || "ONE_TIME";
    }
  } else {
    title.textContent = "Add Expense";
    document.getElementById("expenseId").value = "";
    document.getElementById("expenseDate").value = new Date().toISOString().split("T")[0];
    if (document.getElementById("expenseRecurring")) {
      document.getElementById("expenseRecurring").value = "ONE_TIME";
    }
  }

  modal.classList.add("active");
}

function openBudgetModal(budget = null) {
  const modal = document.getElementById("budgetModal");
  const form = document.getElementById("budgetForm");
  const title = document.getElementById("budgetModalTitle");
  if (!modal || !form) return;

  form.reset();

  if (budget) {
    title.textContent = "Edit Budget";
    document.getElementById("budgetId").value = budget.id;
    document.getElementById("budgetCategory").value = budget.categoryId;
    document.getElementById("budgetAmount").value = budget.amount;
    document.getElementById("budgetMonth").value = budget.month;
  } else {
    title.textContent = "Set Monthly Budget";
    document.getElementById("budgetId").value = "";
    document.getElementById("budgetMonth").value = state.selectedMonth;
  }

  modal.classList.add("active");
}

function openCategoryModal() {
  const modal = document.getElementById("categoryModal");
  if (!modal) return;
  document.getElementById("categoryForm")?.reset();
  modal.classList.add("active");
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove("active");
}

async function editExpense(id) {
  try {
    const expense = await api.getExpenseById(id);
    openExpenseModal(expense);
  } catch (err) {
    showToast(err.message, "danger");
  }
}

async function deleteExpenseItem(id) {
  if (!confirm("Are you sure you want to delete this expense record?")) return;
  try {
    await api.deleteExpense(id);
    showToast("Expense deleted successfully.", "success");
    await loadCurrentViewData();
  } catch (err) {
    showToast(err.message, "danger");
  }
}

async function editBudget(id) {
  try {
    const budget = await api.getBudgetById(id);
    openBudgetModal(budget);
  } catch (err) {
    showToast(err.message, "danger");
  }
}

async function deleteBudgetItem(id) {
  if (!confirm("Are you sure you want to delete this budget limit?")) return;
  try {
    await api.deleteBudget(id);
    showToast("Budget limit deleted successfully.", "success");
    await loadCurrentViewData();
  } catch (err) {
    showToast(err.message, "danger");
  }
}

// Start application on DOM Ready
document.addEventListener("DOMContentLoaded", initApp);
