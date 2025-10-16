const API_BASE = "http://localhost:8080/api";

const el = id => document.getElementById(id);
const loginScreen = el("login-screen");
const dashboard = el("dashboard");
const loginBtn = el("btn-login");
const appPwdInput = el("app-password");
const loginMsg = el("login-msg");
const balVal = el("bal-val");
const flowArea = el("flow-area");

async function apiPost(path, body){
  const res = await fetch(API_BASE + path, {
    method:"POST",
    headers: {"Content-Type":"application/json"},
    body: JSON.stringify(body)
  });
  const json = await res.json().catch(()=>({}));
  return {status: res.status, ok: res.ok, data: json};
}

async function apiGet(path){
  const res = await fetch(API_BASE + path);
  const json = await res.json().catch(()=>({}));
  return {status: res.status, ok: res.ok, data: json};
}

async function refreshBalance(){
  try{
    let r = await apiGet("/balance");
    if (r.ok) balVal.innerText = r.data.balance;
  }catch(e){}
}

loginBtn.addEventListener("click", async ()=>{
  const password = appPwdInput.value.trim();
  if (!password) { loginMsg.innerText="Please enter app password"; return; }
  const r = await apiPost("/login", { password });
  if (r.ok) {
    loginScreen.classList.add("hidden");
    dashboard.classList.remove("hidden");
    loginMsg.innerText = "";
    // store session password in memory so that payment also uses it
    window.APP_PASSWORD = password;
    await refreshBalance();
  } else {
    loginMsg.innerText = r.data?.message || "Auth failed";
  }
});

document.querySelectorAll(".actions button").forEach(btn=>{
  btn.addEventListener("click", ()=> handleAction(btn.dataset.action));
});

function handleAction(action){
  flowArea.innerHTML = "";
  if (action === "send") renderSendForm();
  if (action === "recharge") renderRechargeForm();
  if (action === "electricity") renderElectricityForm();
  if (action === "history") renderHistory();
}

function showMessage(text, isError=false){
  const p = document.createElement("p");
  p.innerText = text; p.className = isError ? "msg" : "small";
  flowArea.prepend(p);
  setTimeout(()=> p.remove(),4000);
}

function renderSendForm(){
  flowArea.innerHTML = `
    <h3>Send Money</h3>
    <input id="to-upi" placeholder="Recipient UPI ID / Account" />
    <input id="amount" type="number" placeholder="Amount (₹)" />
    <input id="pay-password" type="password" placeholder="Enter payment password" />
    <button id="do-pay">Pay</button>
  `;
  el("do-pay").addEventListener("click", async ()=>{
    const target = el("to-upi").value.trim();
    const amount = parseInt(el("amount").value || "0");
    const pwd = el("pay-password").value;
    if (!target || !amount || !pwd) { showMessage("Fill all fields", true); return; }
    const r = await apiPost("/pay", { password: pwd, type: "SEND", target, amount: amount });
    if (r.ok) {
      showMessage("Payment successful ✅");
      await refreshBalance();
    } else {
      showMessage(r.data?.message || "Payment failed", true);
    }
  });
}

function renderRechargeForm(){
  flowArea.innerHTML = `
    <h3>Mobile Recharge</h3>
    <input id="mobile" placeholder="Mobile number" />
    <input id="rech-amount" type="number" placeholder="Amount (₹)" />
    <input id="pay-password-2" type="password" placeholder="Enter payment password" />
    <button id="do-rech">Recharge</button>
  `;
  el("do-rech").addEventListener("click", async ()=>{
    const mobile = el("mobile").value.trim();
    const amount = parseInt(el("rech-amount").value || "0");
    const pwd = el("pay-password-2").value;
    if (!mobile || !amount || !pwd) { showMessage("Fill all fields", true); return; }
    const r = await apiPost("/pay", { password: pwd, type: "RECHARGE", target: mobile, amount });
    if (r.ok) {
      showMessage("Recharge successful ✅");
      await refreshBalance();
    } else {
      showMessage(r.data?.message || "Recharge failed", true);
    }
  });
}

function renderElectricityForm(){
  flowArea.innerHTML = `
    <h3>Electricity Bill Payment</h3>
    <input id="consumer" placeholder="Consumer / Bill ID" />
    <input id="elec-amount" type="number" placeholder="Amount (₹)" />
    <input id="pay-password-3" type="password" placeholder="Enter payment password" />
    <button id="do-elec">Pay Bill</button>
  `;
  el("do-elec").addEventListener("click", async ()=>{
    const consumer = el("consumer").value.trim();
    const amount = parseInt(el("elec-amount").value || "0");
    const pwd = el("pay-password-3").value;
    if (!consumer || !amount || !pwd) { showMessage("Fill all fields", true); return; }
    const r = await apiPost("/pay", { password: pwd, type: "ELECTRICITY", target: consumer, amount });
    if (r.ok) {
      showMessage("Bill payment successful ✅");
      await refreshBalance();
    } else {
      showMessage(r.data?.message || "Payment failed", true);
    }
  });
}

async function renderHistory(){
  flowArea.innerHTML = `<h3>Transaction History</h3><div class="tx-list" id="tx-list"></div>`;
  const wrapper = el("tx-list");
  const r = await apiGet("/transactions");
  if (!r.ok) { wrapper.innerText = "Could not load transactions"; return; }
  const txs = r.data;
  if (!txs.length) { wrapper.innerHTML = "<div class='small'>No transactions yet.</div>"; return; }
  wrapper.innerHTML = txs.map(t => `
    <div class="tx-item">
      <div>
        <div><strong>${t.type}</strong> • ${t.target}</div>
        <div class="small">${new Date(t.createdAt).toLocaleString()}</div>
      </div>
      <div style="text-align:right">
        <div>₹ ${t.amount}</div>
        <div class="small">${t.status}</div>
      </div>
    </div>
  `).join("");
}
