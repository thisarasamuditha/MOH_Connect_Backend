#!/usr/bin/env node
/**
 * Removes the demo data created by seed-demo-data.mjs, using the IDs it saved
 * in demo-seed-output.json.
 *
 *   node cleanup-demo-data.mjs
 *
 * - Clinic sessions are deleted (midwife).
 * - The mother is deleted (midwife); the backend cascades her pregnancy, ANC
 *   records, child, growth visits, vaccinations, Thriposha issues and notifications.
 * - Doctor, midwife and admin accounts are deactivated (admin soft delete), so
 *   their usernames stay taken: re-seed with DEMO_SUFFIX=<number> afterwards.
 * - The Triposha stock batch and the demo PHM area are left in place.
 */
import { readFileSync, renameSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const OUTPUT_FILE = join(dirname(fileURLToPath(import.meta.url)), "demo-seed-output.json");

let seed;
try {
  seed = JSON.parse(readFileSync(OUTPUT_FILE, "utf8"));
} catch {
  console.error(`Cannot read ${OUTPUT_FILE}. Nothing to clean up.`);
  process.exit(1);
}
const BASE = process.env.API_BASE_URL || seed.apiBaseUrl;
const { accounts, ids, password } = seed;

async function api(method, path, token, body) {
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const text = await res.text();
  if (!res.ok) throw new Error(`${method} ${path} -> HTTP ${res.status}: ${text}`);
  return text ? JSON.parse(text) : null;
}

async function attempt(label, fn) {
  try {
    await fn();
    console.log(`  ✓ ${label}`);
  } catch (e) {
    console.log(`  ! ${label}: ${e.message}`);
  }
}

const login = async (username) => (await api("POST", "/auth/login", null, { email: username, password })).token;

console.log(`Cleaning up demo data at ${BASE}`);

const midwifeToken = await login(accounts.midwife.username).catch(() => null);
if (midwifeToken) {
  for (const id of ids.sessions || []) {
    await attempt(`delete session ${id}`, () => api("DELETE", `/sessions/${id}`, midwifeToken));
  }
  if (ids.motherId) {
    await attempt(`delete mother ${ids.motherId} and her clinical records`, () => api("DELETE", `/mothers/${ids.motherId}`, midwifeToken));
  }
} else {
  console.log("  ! could not log in as the demo midwife – sessions and mother not removed");
}

const adminToken = await login(accounts.admin.username).catch(() => null);
if (adminToken) {
  for (const [label, id] of [["doctor", ids.doctorUserId], ["midwife", ids.midwifeUserId], ["admin", ids.adminUserId]]) {
    if (id) await attempt(`deactivate ${label} account (user ${id})`, () => api("DELETE", `/auth/users/${id}`, adminToken));
  }
} else {
  console.log("  ! could not log in as the demo admin – staff accounts not deactivated");
}

renameSync(OUTPUT_FILE, OUTPUT_FILE.replace(/\.json$/, `.cleaned-${Date.now()}.json`));
console.log("Done. Re-seed with a new DEMO_SUFFIX if you need demo accounts again.");
